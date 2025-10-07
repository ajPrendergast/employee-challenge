package com.reliaquest.api.service;

import com.reliaquest.api.config.RetryableApiCall;
import com.reliaquest.api.dto.CreateEmployeeInput;
import com.reliaquest.api.dto.EmployeeApiResponse;
import com.reliaquest.api.dto.MockEmployeeDto;
import com.reliaquest.api.model.Employee;
import java.util.HashMap;
import java.util.List;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.context.annotation.Lazy;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpStatus;
import org.springframework.retry.annotation.Recover;
import org.springframework.stereotype.Service;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestClient;

@Service
@Slf4j
public class EmployeeService {

    private final RestClient restClient;
    private final EmployeeService self;

    public EmployeeService(
            @Value("${employee.api.base-url:http://localhost:8112}") String baseUrl, @Lazy EmployeeService self) {
        this.restClient = RestClient.builder().baseUrl(baseUrl).build();
        this.self = self;
    }

    @RetryableApiCall
    @Cacheable("employees")
    public List<Employee> getAllEmployees() {
        log.info("Attempting to fetch all employees from external API");

        try {
            EmployeeApiResponse<List<MockEmployeeDto>> response = restClient
                    .get()
                    .uri("/api/v1/employee")
                    .retrieve()
                    .body(new ParameterizedTypeReference<EmployeeApiResponse<List<MockEmployeeDto>>>() {});


            List<Employee> employees =
                    response.getData().stream().map(MockEmployeeDto::toEmployee).toList();

            log.info("Successfully fetched {} employees", employees.size());
            return employees;
        } catch (HttpClientErrorException.TooManyRequests e) {
            log.warn("Rate limit hit (429) - retry will be attempted");
            throw e;
        }
    }

    @Recover
    public List<Employee> recoverGetAllEmployees(HttpClientErrorException.TooManyRequests e) {
        log.error("Failed to fetch employees after all retry attempts - rate limit still active");
        throw e;
    }

    public List<Employee> searchEmployeesByName(String searchString) {
        log.info("Searching employees by name: {}", searchString);
        return self.getAllEmployees().stream()
                .filter(emp -> emp.getName().toLowerCase().contains(searchString.toLowerCase()))
                .toList();
    }

    public Employee getEmployeeById(String id) {
        log.info("Fetching employee by ID: {}", id);

        try {
            return fetchEmployeeByIdFromApi(id);
        } catch (HttpClientErrorException e) {
            log.warn("API error fetching employee by ID ({}), falling back to cache", e.getStatusCode());
            return findEmployeeInCache(id);
        }
    }

    private Employee fetchEmployeeByIdFromApi(String id) {
        EmployeeApiResponse<MockEmployeeDto> response = restClient
                .get()
                .uri("/api/v1/employee/{id}", id)
                .retrieve()
                .body(new ParameterizedTypeReference<EmployeeApiResponse<MockEmployeeDto>>() {});

        if (response == null || response.getData() == null) {
            log.warn("Received null response for employee ID: {}", id);
            throw new IllegalArgumentException("Employee not found with id: " + id);
        }

        Employee employee = response.getData().toEmployee();
        log.info("Successfully fetched employee: {}", employee.getName());
        return employee;
    }

    private Employee findEmployeeInCache(String id) {
        List<Employee> cachedEmployees = self.getAllEmployees();
        log.info("Searching for employee {} in cache with {} employees", id, cachedEmployees.size());

        return cachedEmployees.stream()
                .filter(emp -> emp.getId().toString().equals(id))
                .findFirst()
                .orElseThrow(() -> {
                    log.warn("Employee {} not found in cached list of {} employees", id, cachedEmployees.size());
                    return new IllegalArgumentException("Employee not found with id: " + id);
                });
    }

    public Integer getHighestSalaryOfEmployees() {
        return self.getAllEmployees().stream()
                .map(Employee::getSalary)
                .max(Integer::compareTo)
                .orElse(0);
    }

    public List<String> getTopTenHighestEarningEmployeeNames() {
        return self.getAllEmployees().stream()
                .sorted((e1, e2) -> e2.getSalary().compareTo(e1.getSalary()))
                .limit(10)
                .map(Employee::getName)
                .toList();
    }

    @RetryableApiCall
    public Employee createEmployee(CreateEmployeeInput input) {
        log.info("Attempting to create employee with name: {}", input.getName());

        if (input.getAge() != null) {
            if (input.getAge() < 16) {
                log.warn("Business rule violation: Employee age {} is below minimum of 16", input.getAge());
                throw new IllegalArgumentException("Employee age must be at least 16 years old");
            }
            if (input.getAge() > 75) {
                log.warn("Business rule violation: Employee age {} exceeds maximum of 75", input.getAge());
                throw new IllegalArgumentException("Employee age must not exceed 75 years old");
            }
        }

        try {
            EmployeeApiResponse<MockEmployeeDto> response = restClient
                    .post()
                    .uri("/api/v1/employee")
                    .body(input)
                    .retrieve()
                    .body(new ParameterizedTypeReference<EmployeeApiResponse<MockEmployeeDto>>() {});

            if (response == null || response.getData() == null) {
                log.error("Failed to create employee - null response");
                throw new IllegalStateException("Failed to create employee");
            }

            Employee createdEmployee = response.getData().toEmployee();
            log.info("Successfully created employee with id: {}", createdEmployee.getId());
            self.evictEmployeeCache();
            return createdEmployee;
        } catch (HttpClientErrorException.TooManyRequests e) {
            log.warn("Rate limit hit (429) while creating employee - retry will be attempted");
            throw e;
        }
    }

    @Recover
    public Employee recoverCreateEmployee(HttpClientErrorException.TooManyRequests e, CreateEmployeeInput input) {
        log.error("Failed to create employee after all retry attempts - rate limit still active");
        throw e;
    }

    public String deleteEmployeeById(String id) {
        log.info("Attempting to delete employee with id: {}", id);

        try {
            EmployeeApiResponse<MockEmployeeDto> response = restClient
                    .get()
                    .uri("/api/v1/employee/{id}", id)
                    .retrieve()
                    .body(new ParameterizedTypeReference<EmployeeApiResponse<MockEmployeeDto>>() {});

            if (response == null || response.getData() == null) {
                log.warn("Employee does not exist with id: {}", id);
                throw new IllegalStateException("Employee does not exist");
            }

            String employeeName = response.getData().toEmployee().getName();
            return self.performDelete(employeeName, id);
        } catch (HttpClientErrorException.NotFound e) {
            log.warn("Employee does not exist with id: {}", id);
            throw new IllegalStateException("Employee does not exist", e);
        }
    }

    @RetryableApiCall
    String performDelete(String employeeName, String id) {
        var deleteRequest = new HashMap<String, String>();
        deleteRequest.put("name", employeeName);

        restClient
                .method(org.springframework.http.HttpMethod.DELETE)
                .uri("/api/v1/employee")
                .body(deleteRequest)
                .retrieve()
                .toBodilessEntity();
        log.info("Successfully deleted employee with id: {}", id);
        self.evictEmployeeCache();
        return employeeName;
    }

    @Recover
    String recoverPerformDelete(HttpClientErrorException.TooManyRequests e, String employeeName, String id) {
        log.error("Failed to delete employee after all retry attempts - rate limit still active");
        throw e;
    }

    @CacheEvict(value = "employees", allEntries = true)
    void evictEmployeeCache() {
        log.info("Cache eviction - clearing employees cache");
    }
}
