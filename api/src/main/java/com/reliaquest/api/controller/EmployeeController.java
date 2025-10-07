package com.reliaquest.api.controller;

import com.reliaquest.api.dto.CreateEmployeeInput;
import com.reliaquest.api.model.Employee;
import com.reliaquest.api.service.EmployeeService;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestClientException;

@RestController
@RequestMapping("/api/v1/employee")
@RequiredArgsConstructor
@Slf4j
public class EmployeeController implements IEmployeeController<Employee, CreateEmployeeInput> {

    private static final String UUID_PATTERN = "^[0-9a-f]{8}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{12}$";
    private static final String NAME_SEARCH_PATTERN = "^[a-zA-Z.]{1,100}$";
    private static final String NAME_PATTERN = "^[a-zA-Z\\s.'-]{1,100}$";

    private final EmployeeService employeeService;

    @Override
    public ResponseEntity<List<Employee>> getAllEmployees() {
        log.info("GET /api/v1/employee - Getting all employees");
        try {
            List<Employee> employees = employeeService.getAllEmployees();
            log.info("Returning {} employees", employees.size());
            return ResponseEntity.ok(employees);
        } catch (HttpClientErrorException e) {
            if (e.getStatusCode() == HttpStatus.TOO_MANY_REQUESTS) {
                log.error("Rate limit exceeded while fetching employees", e);
                return ResponseEntity.status(HttpStatus.TOO_MANY_REQUESTS).build();
            }
            log.error("HTTP client error fetching all employees", e);
            return ResponseEntity.status(HttpStatus.BAD_GATEWAY).build();
        } catch (RestClientException e) {
            log.error("API error fetching all employees", e);
            return ResponseEntity.status(HttpStatus.BAD_GATEWAY).build();
        } catch (Exception e) {
            log.error("Unexpected error fetching all employees", e);
            return ResponseEntity.internalServerError().build();
        }
    }

    @Override
    public ResponseEntity<List<Employee>> getEmployeesByNameSearch(String searchString) {
        log.info("GET /api/v1/employee/search/{} - Searching employees", searchString);

        if (searchString == null || !searchString.matches(NAME_SEARCH_PATTERN)) {
            log.warn("Invalid search string: {}", searchString);
            return ResponseEntity.badRequest().build();
        }

        try {
            List<Employee> employees = employeeService.searchEmployeesByName(searchString);
            log.info("Found {} employees matching '{}'", employees.size(), searchString);
            return ResponseEntity.ok(employees);
        } catch (HttpClientErrorException e) {
            if (e.getStatusCode() == HttpStatus.TOO_MANY_REQUESTS) {
                log.error("Rate limit exceeded while searching employees", e);
                return ResponseEntity.status(HttpStatus.TOO_MANY_REQUESTS).build();
            }
            log.error("HTTP client error searching employees by name '{}'", searchString, e);
            return ResponseEntity.status(HttpStatus.BAD_GATEWAY).build();
        } catch (RestClientException e) {
            log.error("API error searching employees by name '{}'", searchString, e);
            return ResponseEntity.status(HttpStatus.BAD_GATEWAY).build();
        } catch (Exception e) {
            log.error("Unexpected error searching employees by name '{}'", searchString, e);
            return ResponseEntity.internalServerError().build();
        }
    }

    @Override
    public ResponseEntity<Employee> getEmployeeById(String id) {
        log.info("GET /api/v1/employee/{} - Getting employee by id", id);

        if (id == null || !id.matches(UUID_PATTERN)) {
            log.warn("Invalid UUID format for id: {}", id);
            return ResponseEntity.badRequest().build();
        }

        try {
            Employee employee = employeeService.getEmployeeById(id);
            log.info("Successfully retrieved employee with id: {}", id);
            return ResponseEntity.ok(employee);
        } catch (IllegalArgumentException e) {
            log.warn("Employee not found with id: {}", id);
            return ResponseEntity.notFound().build();
        } catch (HttpClientErrorException e) {
            if (e.getStatusCode() == HttpStatus.TOO_MANY_REQUESTS) {
                log.error("Rate limit exceeded while fetching employee by id", e);
                return ResponseEntity.status(HttpStatus.TOO_MANY_REQUESTS).build();
            }
            log.error("HTTP client error fetching employee with id '{}'", id, e);
            return ResponseEntity.status(HttpStatus.BAD_GATEWAY).build();
        } catch (RestClientException e) {
            log.error("API error fetching employee with id '{}'", id, e);
            return ResponseEntity.status(HttpStatus.BAD_GATEWAY).build();
        } catch (Exception e) {
            log.error("Unexpected error fetching employee with id '{}'", id, e);
            return ResponseEntity.internalServerError().build();
        }
    }

    @Override
    public ResponseEntity<Integer> getHighestSalaryOfEmployees() {
        log.info("GET /api/v1/employee/highest-salary - Getting highest salary of employees");
        try {
            Integer highestSalary = employeeService.getHighestSalaryOfEmployees();
            log.info("Successfully retrieved highest salary: {}", highestSalary);
            return ResponseEntity.ok(highestSalary);
        } catch (HttpClientErrorException e) {
            if (e.getStatusCode() == HttpStatus.TOO_MANY_REQUESTS) {
                log.error("Rate limit exceeded while fetching highest salary", e);
                return ResponseEntity.status(HttpStatus.TOO_MANY_REQUESTS).build();
            }
            log.error("HTTP client error fetching highest salary", e);
            return ResponseEntity.status(HttpStatus.BAD_GATEWAY).build();
        } catch (RestClientException e) {
            log.error("API error fetching highest salary", e);
            return ResponseEntity.status(HttpStatus.BAD_GATEWAY).build();
        } catch (Exception e) {
            log.error("Unexpected error fetching highest salary", e);
            return ResponseEntity.internalServerError().build();
        }
    }

    @Override
    public ResponseEntity<List<String>> getTopTenHighestEarningEmployeeNames() {
        log.info("GET /api/v1/employee/top-ten-highest-earning - Getting top ten highest earning employee names");
        try {
            List<String> topTenNames = employeeService.getTopTenHighestEarningEmployeeNames();
            log.info("Successfully retrieved {} top earning employee names", topTenNames.size());
            return ResponseEntity.ok(topTenNames);
        } catch (HttpClientErrorException e) {
            if (e.getStatusCode() == HttpStatus.TOO_MANY_REQUESTS) {
                log.error("Rate limit exceeded while fetching top earners", e);
                return ResponseEntity.status(HttpStatus.TOO_MANY_REQUESTS).build();
            }
            log.error("HTTP client error fetching top earning employees", e);
            return ResponseEntity.status(HttpStatus.BAD_GATEWAY).build();
        } catch (RestClientException e) {
            log.error("API error fetching top earning employees", e);
            return ResponseEntity.status(HttpStatus.BAD_GATEWAY).build();
        } catch (Exception e) {
            log.error("Unexpected error fetching top earning employees", e);
            return ResponseEntity.internalServerError().build();
        }
    }

    @Override
    public ResponseEntity<Employee> createEmployee(CreateEmployeeInput employeeInput) {
        log.info("POST /api/v1/employee - Creating a new employee");

        if (employeeInput.getName() != null && !employeeInput.getName().matches(NAME_SEARCH_PATTERN)) {
            log.warn("Invalid employee name format: {}", employeeInput.getName());
            return ResponseEntity.badRequest().build();
        }

        if (employeeInput.getTitle() != null && !employeeInput.getTitle().matches(NAME_PATTERN)) {
            log.warn("Invalid employee title format: {}", employeeInput.getTitle());
            return ResponseEntity.badRequest().build();
        }

        try {
            Employee createdEmployee = employeeService.createEmployee(employeeInput);
            log.info("Successfully created employee with id: {}", createdEmployee.getId());
            return ResponseEntity.status(HttpStatus.CREATED).body(createdEmployee);
        } catch (IllegalArgumentException e) {
            log.warn("Business rule validation failed: {}", e.getMessage());
            return ResponseEntity.badRequest().build();
        } catch (IllegalStateException e) {
            log.error("Failed to create employee - invalid state", e);
            return ResponseEntity.status(HttpStatus.CONFLICT).build();
        } catch (HttpClientErrorException e) {
            if (e.getStatusCode() == HttpStatus.TOO_MANY_REQUESTS) {
                log.error("Rate limit exceeded while creating employee", e);
                return ResponseEntity.status(HttpStatus.TOO_MANY_REQUESTS).build();
            }
            log.error("HTTP client error creating employee", e);
            return ResponseEntity.status(HttpStatus.BAD_GATEWAY).build();
        } catch (RestClientException e) {
            log.error("API error creating employee", e);
            return ResponseEntity.status(HttpStatus.BAD_GATEWAY).build();
        } catch (Exception e) {
            log.error("Unexpected error creating employee", e);
            return ResponseEntity.internalServerError().build();
        }
    }

    @Override
    public ResponseEntity<String> deleteEmployeeById(String id) {
        log.info("DELETE /api/v1/employee/{} - Deleting employee by id", id);

        if (id == null || !id.matches(UUID_PATTERN)) {
            log.warn("Invalid UUID format for id: {}", id);
            return ResponseEntity.badRequest().build();
        }

        try {
            String deletedEmployeeName = employeeService.deleteEmployeeById(id);
            log.info("Successfully deleted employee with id: {}", id);
            return ResponseEntity.ok(deletedEmployeeName);
        } catch (IllegalStateException e) {
            log.warn("Employee not found with id: {}", id);
            return ResponseEntity.notFound().build();
        } catch (HttpClientErrorException e) {
            if (e.getStatusCode() == HttpStatus.TOO_MANY_REQUESTS) {
                log.error("Rate limit exceeded while deleting employee", e);
                return ResponseEntity.status(HttpStatus.TOO_MANY_REQUESTS).build();
            }
            log.error("HTTP client error deleting employee with id '{}'", id, e);
            return ResponseEntity.status(HttpStatus.BAD_GATEWAY).build();
        } catch (RestClientException e) {
            log.error("API error deleting employee with id '{}'", id, e);
            return ResponseEntity.status(HttpStatus.BAD_GATEWAY).build();
        } catch (Exception e) {
            log.error("Unexpected error deleting employee with id '{}'", id, e);
            return ResponseEntity.internalServerError().build();
        }
    }
}
