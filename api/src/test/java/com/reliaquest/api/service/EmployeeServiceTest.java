package com.reliaquest.api.service;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

import com.reliaquest.api.dto.CreateEmployeeInput;
import com.reliaquest.api.dto.EmployeeApiResponse;
import com.reliaquest.api.dto.MockEmployeeDto;
import com.reliaquest.api.model.Employee;
import java.util.List;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.web.client.RestClient;

@ExtendWith(MockitoExtension.class)
class EmployeeServiceTest {

    @Mock
    private RestClient restClient;

    @Mock
    private RestClient.RequestHeadersUriSpec requestHeadersUriSpec;

    @Mock
    private RestClient.RequestBodyUriSpec requestBodyUriSpec;

    @Mock
    private RestClient.RequestBodySpec requestBodySpec;

    @Mock
    private RestClient.ResponseSpec responseSpec;

    private EmployeeService employeeService;

    @BeforeEach
    void setUp() {
        employeeService = new EmployeeService("http://localhost:8112", null);
        ReflectionTestUtils.setField(employeeService, "restClient", restClient);
        ReflectionTestUtils.setField(employeeService, "self", employeeService);
    }

    // getAllEmployees tests
    @Test
    void getAllEmployees_shouldReturnListOfEmployees() {
        // Arrange
        UUID employeeId = UUID.randomUUID();
        MockEmployeeDto mockEmployee = new MockEmployeeDto();
        mockEmployee.setId(employeeId);
        mockEmployee.setEmployeeName("John Doe");
        mockEmployee.setEmployeeSalary(75000);
        mockEmployee.setEmployeeAge(30);
        mockEmployee.setEmployeeTitle("Software Engineer");
        mockEmployee.setEmployeeEmail("john.doe@company.com");

        EmployeeApiResponse<List<MockEmployeeDto>> apiResponse = new EmployeeApiResponse<>();
        apiResponse.setData(List.of(mockEmployee));
        apiResponse.setStatus("Successfully processed request.");

        when(restClient.get()).thenReturn(requestHeadersUriSpec);
        when(requestHeadersUriSpec.uri(anyString())).thenReturn(requestHeadersUriSpec);
        when(requestHeadersUriSpec.retrieve()).thenReturn(responseSpec);
        when(responseSpec.body(any(ParameterizedTypeReference.class))).thenReturn(apiResponse);

        // Act
        List<Employee> employees = employeeService.getAllEmployees();

        // Assert
        assertNotNull(employees);
        assertEquals(1, employees.size());
        Employee employee = employees.get(0);
        assertEquals(employeeId, employee.getId());
        assertEquals("John Doe", employee.getName());
        assertEquals(75000, employee.getSalary());
        assertEquals(30, employee.getAge());
        assertEquals("Software Engineer", employee.getTitle());
        assertEquals("john.doe@company.com", employee.getEmail());

        verify(restClient).get();
        verify(requestHeadersUriSpec).uri("/api/v1/employee");
    }

    @Test
    void getAllEmployees_shouldReturnEmptyListWhenResponseIsNull() {
        // Arrange
        when(restClient.get()).thenReturn(requestHeadersUriSpec);
        when(requestHeadersUriSpec.uri(anyString())).thenReturn(requestHeadersUriSpec);
        when(requestHeadersUriSpec.retrieve()).thenReturn(responseSpec);
        when(responseSpec.body(any(ParameterizedTypeReference.class))).thenReturn(null);

        // Act
        List<Employee> employees = employeeService.getAllEmployees();

        // Assert
        assertNotNull(employees);
        assertTrue(employees.isEmpty());
    }

    @Test
    void getAllEmployees_shouldReturnEmptyListWhenDataIsNull() {
        // Arrange
        EmployeeApiResponse<List<MockEmployeeDto>> apiResponse = new EmployeeApiResponse<>();
        apiResponse.setData(null);
        apiResponse.setStatus("Successfully processed request.");

        when(restClient.get()).thenReturn(requestHeadersUriSpec);
        when(requestHeadersUriSpec.uri(anyString())).thenReturn(requestHeadersUriSpec);
        when(requestHeadersUriSpec.retrieve()).thenReturn(responseSpec);
        when(responseSpec.body(any(ParameterizedTypeReference.class))).thenReturn(apiResponse);

        // Act
        List<Employee> employees = employeeService.getAllEmployees();

        // Assert
        assertNotNull(employees);
        assertTrue(employees.isEmpty());
    }

    @Test
    void getAllEmployees_shouldReturnMultipleEmployees() {
        // Arrange
        MockEmployeeDto mockEmployee1 = new MockEmployeeDto();
        mockEmployee1.setId(UUID.randomUUID());
        mockEmployee1.setEmployeeName("John Doe");
        mockEmployee1.setEmployeeSalary(75000);
        mockEmployee1.setEmployeeAge(30);
        mockEmployee1.setEmployeeTitle("Software Engineer");
        mockEmployee1.setEmployeeEmail("john@company.com");

        MockEmployeeDto mockEmployee2 = new MockEmployeeDto();
        mockEmployee2.setId(UUID.randomUUID());
        mockEmployee2.setEmployeeName("Jane Smith");
        mockEmployee2.setEmployeeSalary(85000);
        mockEmployee2.setEmployeeAge(35);
        mockEmployee2.setEmployeeTitle("Senior Engineer");
        mockEmployee2.setEmployeeEmail("jane@company.com");

        EmployeeApiResponse<List<MockEmployeeDto>> apiResponse = new EmployeeApiResponse<>();
        apiResponse.setData(List.of(mockEmployee1, mockEmployee2));
        apiResponse.setStatus("Successfully processed request.");

        when(restClient.get()).thenReturn(requestHeadersUriSpec);
        when(requestHeadersUriSpec.uri(anyString())).thenReturn(requestHeadersUriSpec);
        when(requestHeadersUriSpec.retrieve()).thenReturn(responseSpec);
        when(responseSpec.body(any(ParameterizedTypeReference.class))).thenReturn(apiResponse);

        // Act
        List<Employee> employees = employeeService.getAllEmployees();

        // Assert
        assertNotNull(employees);
        assertEquals(2, employees.size());
    }

    // searchEmployeesByName tests
    @Test
    void searchEmployeesByName_shouldReturnMatchingEmployees() {
        // Arrange
        MockEmployeeDto mockEmployee1 = new MockEmployeeDto();
        mockEmployee1.setId(UUID.randomUUID());
        mockEmployee1.setEmployeeName("John Doe");
        mockEmployee1.setEmployeeSalary(75000);
        mockEmployee1.setEmployeeAge(30);
        mockEmployee1.setEmployeeTitle("Software Engineer");
        mockEmployee1.setEmployeeEmail("john@company.com");

        MockEmployeeDto mockEmployee2 = new MockEmployeeDto();
        mockEmployee2.setId(UUID.randomUUID());
        mockEmployee2.setEmployeeName("Jane Smith");
        mockEmployee2.setEmployeeSalary(85000);
        mockEmployee2.setEmployeeAge(35);
        mockEmployee2.setEmployeeTitle("Senior Engineer");
        mockEmployee2.setEmployeeEmail("jane@company.com");

        EmployeeApiResponse<List<MockEmployeeDto>> apiResponse = new EmployeeApiResponse<>();
        apiResponse.setData(List.of(mockEmployee1, mockEmployee2));

        when(restClient.get()).thenReturn(requestHeadersUriSpec);
        when(requestHeadersUriSpec.uri(anyString())).thenReturn(requestHeadersUriSpec);
        when(requestHeadersUriSpec.retrieve()).thenReturn(responseSpec);
        when(responseSpec.body(any(ParameterizedTypeReference.class))).thenReturn(apiResponse);

        // Act
        List<Employee> result = employeeService.searchEmployeesByName("john");

        // Assert
        assertEquals(1, result.size());
        assertEquals("John Doe", result.get(0).getName());
    }

    @Test
    void searchEmployeesByName_shouldBeCaseInsensitive() {
        // Arrange
        MockEmployeeDto mockEmployee = new MockEmployeeDto();
        mockEmployee.setId(UUID.randomUUID());
        mockEmployee.setEmployeeName("John Doe");
        mockEmployee.setEmployeeSalary(75000);
        mockEmployee.setEmployeeAge(30);
        mockEmployee.setEmployeeTitle("Software Engineer");
        mockEmployee.setEmployeeEmail("john@company.com");

        EmployeeApiResponse<List<MockEmployeeDto>> apiResponse = new EmployeeApiResponse<>();
        apiResponse.setData(List.of(mockEmployee));

        when(restClient.get()).thenReturn(requestHeadersUriSpec);
        when(requestHeadersUriSpec.uri(anyString())).thenReturn(requestHeadersUriSpec);
        when(requestHeadersUriSpec.retrieve()).thenReturn(responseSpec);
        when(responseSpec.body(any(ParameterizedTypeReference.class))).thenReturn(apiResponse);

        // Act
        List<Employee> result = employeeService.searchEmployeesByName("JOHN");

        // Assert
        assertEquals(1, result.size());
        assertEquals("John Doe", result.get(0).getName());
    }

    @Test
    void searchEmployeesByName_shouldReturnEmptyListWhenNoMatches() {
        // Arrange
        MockEmployeeDto mockEmployee = new MockEmployeeDto();
        mockEmployee.setId(UUID.randomUUID());
        mockEmployee.setEmployeeName("John Doe");
        mockEmployee.setEmployeeSalary(75000);
        mockEmployee.setEmployeeAge(30);
        mockEmployee.setEmployeeTitle("Software Engineer");
        mockEmployee.setEmployeeEmail("john@company.com");

        EmployeeApiResponse<List<MockEmployeeDto>> apiResponse = new EmployeeApiResponse<>();
        apiResponse.setData(List.of(mockEmployee));

        when(restClient.get()).thenReturn(requestHeadersUriSpec);
        when(requestHeadersUriSpec.uri(anyString())).thenReturn(requestHeadersUriSpec);
        when(requestHeadersUriSpec.retrieve()).thenReturn(responseSpec);
        when(responseSpec.body(any(ParameterizedTypeReference.class))).thenReturn(apiResponse);

        // Act
        List<Employee> result = employeeService.searchEmployeesByName("nonexistent");

        // Assert
        assertTrue(result.isEmpty());
    }

    // getEmployeeById tests
    @Test
    void getEmployeeById_shouldReturnEmployee() {
        // Arrange
        UUID employeeId = UUID.randomUUID();
        MockEmployeeDto mockEmployee = new MockEmployeeDto();
        mockEmployee.setId(employeeId);
        mockEmployee.setEmployeeName("John Doe");
        mockEmployee.setEmployeeSalary(75000);
        mockEmployee.setEmployeeAge(30);
        mockEmployee.setEmployeeTitle("Software Engineer");
        mockEmployee.setEmployeeEmail("john@company.com");

        EmployeeApiResponse<MockEmployeeDto> apiResponse = new EmployeeApiResponse<>();
        apiResponse.setData(mockEmployee);

        when(restClient.get()).thenReturn(requestHeadersUriSpec);
        when(requestHeadersUriSpec.uri(eq("/api/v1/employee/{id}"), eq(employeeId.toString())))
                .thenReturn(requestHeadersUriSpec);
        when(requestHeadersUriSpec.retrieve()).thenReturn(responseSpec);
        when(responseSpec.body(any(ParameterizedTypeReference.class))).thenReturn(apiResponse);

        // Act
        Employee result = employeeService.getEmployeeById(employeeId.toString());

        // Assert
        assertNotNull(result);
        assertEquals(employeeId, result.getId());
        assertEquals("John Doe", result.getName());
    }

    @Test
    void getEmployeeById_shouldThrowExceptionWhenNotFound() {
        // Arrange
        String employeeId = UUID.randomUUID().toString();
        EmployeeApiResponse<MockEmployeeDto> apiResponse = new EmployeeApiResponse<>();
        apiResponse.setData(null);

        when(restClient.get()).thenReturn(requestHeadersUriSpec);
        when(requestHeadersUriSpec.uri(eq("/api/v1/employee/{id}"), eq(employeeId)))
                .thenReturn(requestHeadersUriSpec);
        when(requestHeadersUriSpec.retrieve()).thenReturn(responseSpec);
        when(responseSpec.body(any(ParameterizedTypeReference.class))).thenReturn(apiResponse);

        // Act & Assert
        assertThrows(IllegalArgumentException.class, () -> employeeService.getEmployeeById(employeeId));
    }

    // getHighestSalaryOfEmployees tests
    @Test
    void getHighestSalaryOfEmployees_shouldReturnHighestSalary() {
        // Arrange
        MockEmployeeDto mockEmployee1 = new MockEmployeeDto();
        mockEmployee1.setId(UUID.randomUUID());
        mockEmployee1.setEmployeeName("John Doe");
        mockEmployee1.setEmployeeSalary(75000);
        mockEmployee1.setEmployeeAge(30);
        mockEmployee1.setEmployeeTitle("Software Engineer");
        mockEmployee1.setEmployeeEmail("john@company.com");

        MockEmployeeDto mockEmployee2 = new MockEmployeeDto();
        mockEmployee2.setId(UUID.randomUUID());
        mockEmployee2.setEmployeeName("Jane Smith");
        mockEmployee2.setEmployeeSalary(95000);
        mockEmployee2.setEmployeeAge(35);
        mockEmployee2.setEmployeeTitle("Senior Engineer");
        mockEmployee2.setEmployeeEmail("jane@company.com");

        EmployeeApiResponse<List<MockEmployeeDto>> apiResponse = new EmployeeApiResponse<>();
        apiResponse.setData(List.of(mockEmployee1, mockEmployee2));

        when(restClient.get()).thenReturn(requestHeadersUriSpec);
        when(requestHeadersUriSpec.uri(anyString())).thenReturn(requestHeadersUriSpec);
        when(requestHeadersUriSpec.retrieve()).thenReturn(responseSpec);
        when(responseSpec.body(any(ParameterizedTypeReference.class))).thenReturn(apiResponse);

        // Act
        Integer result = employeeService.getHighestSalaryOfEmployees();

        // Assert
        assertEquals(95000, result);
    }

    @Test
    void getHighestSalaryOfEmployees_shouldReturnZeroWhenNoEmployees() {
        // Arrange
        EmployeeApiResponse<List<MockEmployeeDto>> apiResponse = new EmployeeApiResponse<>();
        apiResponse.setData(List.of());

        when(restClient.get()).thenReturn(requestHeadersUriSpec);
        when(requestHeadersUriSpec.uri(anyString())).thenReturn(requestHeadersUriSpec);
        when(requestHeadersUriSpec.retrieve()).thenReturn(responseSpec);
        when(responseSpec.body(any(ParameterizedTypeReference.class))).thenReturn(apiResponse);

        // Act
        Integer result = employeeService.getHighestSalaryOfEmployees();

        // Assert
        assertEquals(0, result);
    }

    // getTopTenHighestEarningEmployeeNames tests
    @Test
    void getTopTenHighestEarningEmployeeNames_shouldReturnTopTen() {
        // Arrange
        List<MockEmployeeDto> mockEmployees = List.of(
                createMockEmployee("Employee 1", 100000),
                createMockEmployee("Employee 2", 95000),
                createMockEmployee("Employee 3", 90000),
                createMockEmployee("Employee 4", 85000),
                createMockEmployee("Employee 5", 80000),
                createMockEmployee("Employee 6", 75000),
                createMockEmployee("Employee 7", 70000),
                createMockEmployee("Employee 8", 65000),
                createMockEmployee("Employee 9", 60000),
                createMockEmployee("Employee 10", 55000),
                createMockEmployee("Employee 11", 50000),
                createMockEmployee("Employee 12", 45000));

        EmployeeApiResponse<List<MockEmployeeDto>> apiResponse = new EmployeeApiResponse<>();
        apiResponse.setData(mockEmployees);

        when(restClient.get()).thenReturn(requestHeadersUriSpec);
        when(requestHeadersUriSpec.uri(anyString())).thenReturn(requestHeadersUriSpec);
        when(requestHeadersUriSpec.retrieve()).thenReturn(responseSpec);
        when(responseSpec.body(any(ParameterizedTypeReference.class))).thenReturn(apiResponse);

        // Act
        List<String> result = employeeService.getTopTenHighestEarningEmployeeNames();

        // Assert
        assertEquals(10, result.size());
        assertEquals("Employee 1", result.get(0));
        assertEquals("Employee 10", result.get(9));
    }

    @Test
    void getTopTenHighestEarningEmployeeNames_shouldReturnAllWhenLessThanTen() {
        // Arrange
        List<MockEmployeeDto> mockEmployees =
                List.of(createMockEmployee("Employee 1", 100000), createMockEmployee("Employee 2", 95000));

        EmployeeApiResponse<List<MockEmployeeDto>> apiResponse = new EmployeeApiResponse<>();
        apiResponse.setData(mockEmployees);

        when(restClient.get()).thenReturn(requestHeadersUriSpec);
        when(requestHeadersUriSpec.uri(anyString())).thenReturn(requestHeadersUriSpec);
        when(requestHeadersUriSpec.retrieve()).thenReturn(responseSpec);
        when(responseSpec.body(any(ParameterizedTypeReference.class))).thenReturn(apiResponse);

        // Act
        List<String> result = employeeService.getTopTenHighestEarningEmployeeNames();

        // Assert
        assertEquals(2, result.size());
    }

    // createEmployee tests
    @Test
    void createEmployee_shouldThrowExceptionWhenAgeBelowMinimum() {
        // Arrange
        CreateEmployeeInput input = CreateEmployeeInput.builder()
                .name("John Doe")
                .salary(75000)
                .age(15)
                .title("Software Engineer")
                .build();

        // Act & Assert
        IllegalArgumentException exception =
                assertThrows(IllegalArgumentException.class, () -> employeeService.createEmployee(input));
        assertEquals("Employee age must be at least 16 years old", exception.getMessage());
    }

    @Test
    void createEmployee_shouldThrowExceptionWhenAgeAboveMaximum() {
        // Arrange
        CreateEmployeeInput input = CreateEmployeeInput.builder()
                .name("John Doe")
                .salary(75000)
                .age(76)
                .title("Software Engineer")
                .build();

        // Act & Assert
        IllegalArgumentException exception =
                assertThrows(IllegalArgumentException.class, () -> employeeService.createEmployee(input));
        assertEquals("Employee age must not exceed 75 years old", exception.getMessage());
    }

    // Helper method
    private MockEmployeeDto createMockEmployee(String name, Integer salary) {
        MockEmployeeDto mockEmployee = new MockEmployeeDto();
        mockEmployee.setId(UUID.randomUUID());
        mockEmployee.setEmployeeName(name);
        mockEmployee.setEmployeeSalary(salary);
        mockEmployee.setEmployeeAge(30);
        mockEmployee.setEmployeeTitle("Engineer");
        mockEmployee.setEmployeeEmail(name.toLowerCase().replace(" ", ".") + "@company.com");
        return mockEmployee;
    }
}
