package com.reliaquest.api.controller;

import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.reliaquest.api.dto.CreateEmployeeInput;
import com.reliaquest.api.model.Employee;
import com.reliaquest.api.service.EmployeeService;
import java.util.List;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.web.client.HttpClientErrorException;

@WebMvcTest(EmployeeController.class)
class EmployeeControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private EmployeeService employeeService;

    // getAllEmployees tests
    @Test
    void getAllEmployees_shouldReturnListOfEmployees() throws Exception {
        // Arrange
        Employee employee1 = Employee.builder()
                .id(UUID.randomUUID())
                .name("John Doe")
                .salary(75000)
                .age(30)
                .title("Software Engineer")
                .email("john@company.com")
                .build();

        Employee employee2 = Employee.builder()
                .id(UUID.randomUUID())
                .name("Jane Smith")
                .salary(85000)
                .age(35)
                .title("Senior Engineer")
                .email("jane@company.com")
                .build();

        List<Employee> employees = List.of(employee1, employee2);
        when(employeeService.getAllEmployees()).thenReturn(employees);

        // Act & Assert
        mockMvc.perform(get("/api/v1/employee"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$.length()").value(2))
                .andExpect(jsonPath("$[0].name").value("John Doe"))
                .andExpect(jsonPath("$[0].salary").value(75000))
                .andExpect(jsonPath("$[0].age").value(30))
                .andExpect(jsonPath("$[0].title").value("Software Engineer"))
                .andExpect(jsonPath("$[0].email").value("john@company.com"))
                .andExpect(jsonPath("$[1].name").value("Jane Smith"))
                .andExpect(jsonPath("$[1].salary").value(85000))
                .andExpect(jsonPath("$[1].age").value(35))
                .andExpect(jsonPath("$[1].title").value("Senior Engineer"))
                .andExpect(jsonPath("$[1].email").value("jane@company.com"));

        verify(employeeService, times(1)).getAllEmployees();
    }

    @Test
    void getAllEmployees_shouldReturnEmptyList() throws Exception {
        // Arrange
        when(employeeService.getAllEmployees()).thenReturn(List.of());

        // Act & Assert
        mockMvc.perform(get("/api/v1/employee"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$.length()").value(0));

        verify(employeeService, times(1)).getAllEmployees();
    }

    @Test
    void getAllEmployees_shouldReturn429WhenRateLimited() throws Exception {
        // Arrange
        when(employeeService.getAllEmployees()).thenThrow(new HttpClientErrorException(HttpStatus.TOO_MANY_REQUESTS));

        // Act & Assert
        mockMvc.perform(get("/api/v1/employee")).andExpect(status().isTooManyRequests());

        verify(employeeService, times(1)).getAllEmployees();
    }

    @Test
    void getAllEmployees_shouldReturn500OnException() throws Exception {
        // Arrange
        when(employeeService.getAllEmployees()).thenThrow(new RuntimeException("Service error"));

        // Act & Assert
        mockMvc.perform(get("/api/v1/employee")).andExpect(status().isInternalServerError());

        verify(employeeService, times(1)).getAllEmployees();
    }

    // getEmployeesByNameSearch tests
    @Test
    void getEmployeesByNameSearch_shouldReturnMatchingEmployees() throws Exception {
        // Arrange
        Employee employee = Employee.builder()
                .id(UUID.randomUUID())
                .name("John Doe")
                .salary(75000)
                .age(30)
                .title("Software Engineer")
                .email("john@company.com")
                .build();

        when(employeeService.searchEmployeesByName("john")).thenReturn(List.of(employee));

        // Act & Assert
        mockMvc.perform(get("/api/v1/employee/search/john"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$.length()").value(1))
                .andExpect(jsonPath("$[0].name").value("John Doe"));

        verify(employeeService, times(1)).searchEmployeesByName("john");
    }

    @Test
    void getEmployeesByNameSearch_shouldReturn429WhenRateLimited() throws Exception {
        // Arrange
        when(employeeService.searchEmployeesByName("john"))
                .thenThrow(new HttpClientErrorException(HttpStatus.TOO_MANY_REQUESTS));

        // Act & Assert
        mockMvc.perform(get("/api/v1/employee/search/john")).andExpect(status().isTooManyRequests());

        verify(employeeService, times(1)).searchEmployeesByName("john");
    }

    @Test
    void getEmployeesByNameSearch_shouldReturn500OnException() throws Exception {
        // Arrange
        when(employeeService.searchEmployeesByName("john")).thenThrow(new RuntimeException("Service error"));

        // Act & Assert
        mockMvc.perform(get("/api/v1/employee/search/john")).andExpect(status().isInternalServerError());

        verify(employeeService, times(1)).searchEmployeesByName("john");
    }

    @Test
    void getEmployeesByNameSearch_shouldReturn400WhenContainsNumbers() throws Exception {
        // Act & Assert
        mockMvc.perform(get("/api/v1/employee/search/john123")).andExpect(status().isBadRequest());

        verify(employeeService, never()).searchEmployeesByName(anyString());
    }

    @Test
    void getEmployeesByNameSearch_shouldReturn400WhenContainsSpecialCharacters() throws Exception {
        // Act & Assert
        mockMvc.perform(get("/api/v1/employee/search/john$doe")).andExpect(status().isBadRequest());

        verify(employeeService, never()).searchEmployeesByName(anyString());
    }

    @Test
    void getEmployeesByNameSearch_shouldReturn400WhenContainsSQLInjection() throws Exception {
        // Act & Assert
        mockMvc.perform(get("/api/v1/employee/search/' OR '1'='1")).andExpect(status().isBadRequest());

        verify(employeeService, never()).searchEmployeesByName(anyString());
    }

    @Test
    void getEmployeesByNameSearch_shouldAcceptValidNameWithPeriods() throws Exception {
        // Arrange
        when(employeeService.searchEmployeesByName("John.Doe")).thenReturn(List.of());

        // Act & Assert
        mockMvc.perform(get("/api/v1/employee/search/John.Doe")).andExpect(status().isOk());

        verify(employeeService, times(1)).searchEmployeesByName("John.Doe");
    }

    // getEmployeeById tests
    @Test
    void getEmployeeById_shouldReturnEmployee() throws Exception {
        // Arrange
        UUID employeeId = UUID.randomUUID();
        Employee employee = Employee.builder()
                .id(employeeId)
                .name("John Doe")
                .salary(75000)
                .age(30)
                .title("Software Engineer")
                .email("john@company.com")
                .build();

        when(employeeService.getEmployeeById(employeeId.toString())).thenReturn(employee);

        // Act & Assert
        mockMvc.perform(get("/api/v1/employee/" + employeeId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(employeeId.toString()))
                .andExpect(jsonPath("$.name").value("John Doe"))
                .andExpect(jsonPath("$.salary").value(75000))
                .andExpect(jsonPath("$.age").value(30))
                .andExpect(jsonPath("$.title").value("Software Engineer"))
                .andExpect(jsonPath("$.email").value("john@company.com"));

        verify(employeeService, times(1)).getEmployeeById(employeeId.toString());
    }

    @Test
    void getEmployeeById_shouldReturn404WhenNotFound() throws Exception {
        // Arrange
        String employeeId = UUID.randomUUID().toString();
        when(employeeService.getEmployeeById(employeeId)).thenThrow(new IllegalArgumentException("Employee not found"));

        // Act & Assert
        mockMvc.perform(get("/api/v1/employee/" + employeeId)).andExpect(status().isNotFound());

        verify(employeeService, times(1)).getEmployeeById(employeeId);
    }

    @Test
    void getEmployeeById_shouldReturn429WhenRateLimited() throws Exception {
        // Arrange
        String employeeId = UUID.randomUUID().toString();
        when(employeeService.getEmployeeById(employeeId))
                .thenThrow(new HttpClientErrorException(HttpStatus.TOO_MANY_REQUESTS));

        // Act & Assert
        mockMvc.perform(get("/api/v1/employee/" + employeeId)).andExpect(status().isTooManyRequests());

        verify(employeeService, times(1)).getEmployeeById(employeeId);
    }

    @Test
    void getEmployeeById_shouldReturn500OnException() throws Exception {
        // Arrange
        String employeeId = UUID.randomUUID().toString();
        when(employeeService.getEmployeeById(employeeId)).thenThrow(new RuntimeException("Service error"));

        // Act & Assert
        mockMvc.perform(get("/api/v1/employee/" + employeeId)).andExpect(status().isInternalServerError());

        verify(employeeService, times(1)).getEmployeeById(employeeId);
    }

    @Test
    void getEmployeeById_shouldReturn400WhenIdIsNotValidUUID() throws Exception {
        // Act & Assert
        mockMvc.perform(get("/api/v1/employee/invalid-id")).andExpect(status().isBadRequest());

        verify(employeeService, never()).getEmployeeById(anyString());
    }

    @Test
    void getEmployeeById_shouldReturn400WhenIdContainsSQLInjection() throws Exception {
        // Act & Assert
        mockMvc.perform(get("/api/v1/employee/' OR '1'='1")).andExpect(status().isBadRequest());

        verify(employeeService, never()).getEmployeeById(anyString());
    }

    // getHighestSalaryOfEmployees tests
    @Test
    void getHighestSalaryOfEmployees_shouldReturnHighestSalary() throws Exception {
        // Arrange
        when(employeeService.getHighestSalaryOfEmployees()).thenReturn(95000);

        // Act & Assert
        mockMvc.perform(get("/api/v1/employee/highestSalary"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").value(95000));

        verify(employeeService, times(1)).getHighestSalaryOfEmployees();
    }

    @Test
    void getHighestSalaryOfEmployees_shouldReturn429WhenRateLimited() throws Exception {
        // Arrange
        when(employeeService.getHighestSalaryOfEmployees())
                .thenThrow(new HttpClientErrorException(HttpStatus.TOO_MANY_REQUESTS));

        // Act & Assert
        mockMvc.perform(get("/api/v1/employee/highestSalary")).andExpect(status().isTooManyRequests());

        verify(employeeService, times(1)).getHighestSalaryOfEmployees();
    }

    @Test
    void getHighestSalaryOfEmployees_shouldReturn500OnException() throws Exception {
        // Arrange
        when(employeeService.getHighestSalaryOfEmployees()).thenThrow(new RuntimeException("Service error"));

        // Act & Assert
        mockMvc.perform(get("/api/v1/employee/highestSalary")).andExpect(status().isInternalServerError());

        verify(employeeService, times(1)).getHighestSalaryOfEmployees();
    }

    // getTopTenHighestEarningEmployeeNames tests
    @Test
    void getTopTenHighestEarningEmployeeNames_shouldReturnTopTen() throws Exception {
        // Arrange
        List<String> topTen = List.of(
                "Employee 1",
                "Employee 2",
                "Employee 3",
                "Employee 4",
                "Employee 5",
                "Employee 6",
                "Employee 7",
                "Employee 8",
                "Employee 9",
                "Employee 10");

        when(employeeService.getTopTenHighestEarningEmployeeNames()).thenReturn(topTen);

        // Act & Assert
        mockMvc.perform(get("/api/v1/employee/topTenHighestEarningEmployeeNames"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$.length()").value(10))
                .andExpect(jsonPath("$[0]").value("Employee 1"))
                .andExpect(jsonPath("$[9]").value("Employee 10"));

        verify(employeeService, times(1)).getTopTenHighestEarningEmployeeNames();
    }

    @Test
    void getTopTenHighestEarningEmployeeNames_shouldReturn429WhenRateLimited() throws Exception {
        // Arrange
        when(employeeService.getTopTenHighestEarningEmployeeNames())
                .thenThrow(new HttpClientErrorException(HttpStatus.TOO_MANY_REQUESTS));

        // Act & Assert
        mockMvc.perform(get("/api/v1/employee/topTenHighestEarningEmployeeNames"))
                .andExpect(status().isTooManyRequests());

        verify(employeeService, times(1)).getTopTenHighestEarningEmployeeNames();
    }

    @Test
    void getTopTenHighestEarningEmployeeNames_shouldReturn500OnException() throws Exception {
        // Arrange
        when(employeeService.getTopTenHighestEarningEmployeeNames()).thenThrow(new RuntimeException("Service error"));

        // Act & Assert
        mockMvc.perform(get("/api/v1/employee/topTenHighestEarningEmployeeNames"))
                .andExpect(status().isInternalServerError());

        verify(employeeService, times(1)).getTopTenHighestEarningEmployeeNames();
    }

    // createEmployee tests
    @Test
    void createEmployee_shouldReturnCreatedEmployee() throws Exception {
        // Arrange
        CreateEmployeeInput input = CreateEmployeeInput.builder()
                .name("John Doe")
                .salary(75000)
                .age(30)
                .title("Software Engineer")
                .build();

        UUID employeeId = UUID.randomUUID();
        Employee createdEmployee = Employee.builder()
                .id(employeeId)
                .name("John Doe")
                .salary(75000)
                .age(30)
                .title("Software Engineer")
                .email("john.doe@company.com")
                .build();

        when(employeeService.createEmployee(any(CreateEmployeeInput.class))).thenReturn(createdEmployee);

        // Act & Assert
        mockMvc.perform(post("/api/v1/employee")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(input)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(employeeId.toString()))
                .andExpect(jsonPath("$.name").value("John Doe"))
                .andExpect(jsonPath("$.salary").value(75000))
                .andExpect(jsonPath("$.age").value(30))
                .andExpect(jsonPath("$.title").value("Software Engineer"));

        verify(employeeService, times(1)).createEmployee(any(CreateEmployeeInput.class));
    }

    @Test
    void createEmployee_shouldReturn429WhenRateLimited() throws Exception {
        // Arrange
        CreateEmployeeInput input = CreateEmployeeInput.builder()
                .name("John Doe")
                .salary(75000)
                .age(30)
                .title("Software Engineer")
                .build();

        when(employeeService.createEmployee(any(CreateEmployeeInput.class)))
                .thenThrow(new HttpClientErrorException(HttpStatus.TOO_MANY_REQUESTS));

        // Act & Assert
        mockMvc.perform(post("/api/v1/employee")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(input)))
                .andExpect(status().isTooManyRequests());

        verify(employeeService, times(1)).createEmployee(any(CreateEmployeeInput.class));
    }

    @Test
    void createEmployee_shouldReturn500OnException() throws Exception {
        // Arrange
        CreateEmployeeInput input = CreateEmployeeInput.builder()
                .name("John Doe")
                .salary(75000)
                .age(30)
                .title("Software Engineer")
                .build();

        when(employeeService.createEmployee(any(CreateEmployeeInput.class)))
                .thenThrow(new RuntimeException("Service error"));

        // Act & Assert
        mockMvc.perform(post("/api/v1/employee")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(input)))
                .andExpect(status().isInternalServerError());

        verify(employeeService, times(1)).createEmployee(any(CreateEmployeeInput.class));
    }

    @Test
    void createEmployee_shouldReturn400WhenNameContainsSQLInjection() throws Exception {
        // Arrange
        CreateEmployeeInput input = CreateEmployeeInput.builder()
                .name("'; DROP TABLE employees--")
                .salary(75000)
                .age(30)
                .title("Software Engineer")
                .build();

        // Act & Assert
        mockMvc.perform(post("/api/v1/employee")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(input)))
                .andExpect(status().isBadRequest());

        verify(employeeService, never()).createEmployee(any(CreateEmployeeInput.class));
    }

    @Test
    void createEmployee_shouldReturn400WhenNameContainsSpecialCharacters() throws Exception {
        // Arrange
        CreateEmployeeInput input = CreateEmployeeInput.builder()
                .name("John<script>alert('xss')</script>")
                .salary(75000)
                .age(30)
                .title("Software Engineer")
                .build();

        // Act & Assert
        mockMvc.perform(post("/api/v1/employee")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(input)))
                .andExpect(status().isBadRequest());

        verify(employeeService, never()).createEmployee(any(CreateEmployeeInput.class));
    }

    @Test
    void createEmployee_shouldReturn400WhenTitleContainsSQLInjection() throws Exception {
        // Arrange
        CreateEmployeeInput input = CreateEmployeeInput.builder()
                .name("John Doe")
                .salary(75000)
                .age(30)
                .title("Engineer'; DELETE FROM users--")
                .build();

        // Act & Assert
        mockMvc.perform(post("/api/v1/employee")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(input)))
                .andExpect(status().isBadRequest());

        verify(employeeService, never()).createEmployee(any(CreateEmployeeInput.class));
    }

    @Test
    void createEmployee_shouldAcceptValidNameWithHyphensAndApostrophes() throws Exception {
        // Arrange
        CreateEmployeeInput input = CreateEmployeeInput.builder()
                .name("Mary-Jane O'Connor")
                .salary(75000)
                .age(30)
                .title("Software Engineer")
                .build();

        UUID employeeId = UUID.randomUUID();
        Employee createdEmployee = Employee.builder()
                .id(employeeId)
                .name("Mary-Jane O'Connor")
                .salary(75000)
                .age(30)
                .title("Software Engineer")
                .email("mary.jane@company.com")
                .build();

        when(employeeService.createEmployee(any(CreateEmployeeInput.class))).thenReturn(createdEmployee);

        // Act & Assert
        mockMvc.perform(post("/api/v1/employee")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(input)))
                .andExpect(status().isCreated());

        verify(employeeService, times(1)).createEmployee(any(CreateEmployeeInput.class));
    }

    @Test
    void createEmployee_shouldReturn400WhenAgeIsBelowMinimum() throws Exception {
        // Arrange
        CreateEmployeeInput input = CreateEmployeeInput.builder()
                .name("John Doe")
                .salary(75000)
                .age(15)
                .title("Software Engineer")
                .build();

        when(employeeService.createEmployee(any(CreateEmployeeInput.class)))
                .thenThrow(new IllegalArgumentException("Employee age must be at least 16 years old"));

        // Act & Assert
        mockMvc.perform(post("/api/v1/employee")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(input)))
                .andExpect(status().isBadRequest());

        verify(employeeService, times(1)).createEmployee(any(CreateEmployeeInput.class));
    }

    @Test
    void createEmployee_shouldReturn400WhenAgeIsAboveMaximum() throws Exception {
        // Arrange
        CreateEmployeeInput input = CreateEmployeeInput.builder()
                .name("John Doe")
                .salary(75000)
                .age(76)
                .title("Software Engineer")
                .build();

        when(employeeService.createEmployee(any(CreateEmployeeInput.class)))
                .thenThrow(new IllegalArgumentException("Employee age must not exceed 75 years old"));

        // Act & Assert
        mockMvc.perform(post("/api/v1/employee")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(input)))
                .andExpect(status().isBadRequest());

        verify(employeeService, times(1)).createEmployee(any(CreateEmployeeInput.class));
    }

    @Test
    void createEmployee_shouldAcceptAgeAtMinimumBoundary() throws Exception {
        // Arrange
        CreateEmployeeInput input = CreateEmployeeInput.builder()
                .name("John Doe")
                .salary(75000)
                .age(16)
                .title("Software Engineer")
                .build();

        UUID employeeId = UUID.randomUUID();
        Employee createdEmployee = Employee.builder()
                .id(employeeId)
                .name("John Doe")
                .salary(75000)
                .age(16)
                .title("Software Engineer")
                .email("john.doe@company.com")
                .build();

        when(employeeService.createEmployee(any(CreateEmployeeInput.class))).thenReturn(createdEmployee);

        // Act & Assert
        mockMvc.perform(post("/api/v1/employee")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(input)))
                .andExpect(status().isCreated());

        verify(employeeService, times(1)).createEmployee(any(CreateEmployeeInput.class));
    }

    @Test
    void createEmployee_shouldAcceptAgeAtMaximumBoundary() throws Exception {
        // Arrange
        CreateEmployeeInput input = CreateEmployeeInput.builder()
                .name("John Doe")
                .salary(75000)
                .age(75)
                .title("Software Engineer")
                .build();

        UUID employeeId = UUID.randomUUID();
        Employee createdEmployee = Employee.builder()
                .id(employeeId)
                .name("John Doe")
                .salary(75000)
                .age(75)
                .title("Software Engineer")
                .email("john.doe@company.com")
                .build();

        when(employeeService.createEmployee(any(CreateEmployeeInput.class))).thenReturn(createdEmployee);

        // Act & Assert
        mockMvc.perform(post("/api/v1/employee")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(input)))
                .andExpect(status().isCreated());

        verify(employeeService, times(1)).createEmployee(any(CreateEmployeeInput.class));
    }

    // deleteEmployeeById tests
    @Test
    void deleteEmployeeById_shouldReturnSuccessMessage() throws Exception {
        // Arrange
        String employeeId = UUID.randomUUID().toString();
        String employeeName = "John Doe";
        when(employeeService.deleteEmployeeById(employeeId)).thenReturn(employeeName);

        // Act & Assert
        mockMvc.perform(delete("/api/v1/employee/" + employeeId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").value(employeeName));

        verify(employeeService, times(1)).deleteEmployeeById(employeeId);
    }

    @Test
    void deleteEmployeeById_shouldReturn429WhenRateLimited() throws Exception {
        // Arrange
        String employeeId = UUID.randomUUID().toString();
        doThrow(new HttpClientErrorException(HttpStatus.TOO_MANY_REQUESTS))
                .when(employeeService)
                .deleteEmployeeById(employeeId);

        // Act & Assert
        mockMvc.perform(delete("/api/v1/employee/" + employeeId)).andExpect(status().isTooManyRequests());

        verify(employeeService, times(1)).deleteEmployeeById(employeeId);
    }

    @Test
    void deleteEmployeeById_shouldReturn500OnException() throws Exception {
        // Arrange
        String employeeId = UUID.randomUUID().toString();
        doThrow(new RuntimeException("Service error")).when(employeeService).deleteEmployeeById(employeeId);

        // Act & Assert
        mockMvc.perform(delete("/api/v1/employee/" + employeeId)).andExpect(status().isInternalServerError());

        verify(employeeService, times(1)).deleteEmployeeById(employeeId);
    }

    @Test
    void deleteEmployeeById_shouldReturn400WhenIdIsNotValidUUID() throws Exception {
        // Act & Assert
        mockMvc.perform(delete("/api/v1/employee/invalid-id")).andExpect(status().isBadRequest());

        verify(employeeService, never()).deleteEmployeeById(anyString());
    }

    @Test
    void deleteEmployeeById_shouldReturn400WhenIdContainsSQLInjection() throws Exception {
        // Act & Assert
        mockMvc.perform(delete("/api/v1/employee/' OR '1'='1")).andExpect(status().isBadRequest());

        verify(employeeService, never()).deleteEmployeeById(anyString());
    }
}
