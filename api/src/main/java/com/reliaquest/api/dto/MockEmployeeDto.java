package com.reliaquest.api.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.reliaquest.api.model.Employee;
import java.util.UUID;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class MockEmployeeDto {

    private UUID id;

    @JsonProperty("employee_name")
    private String employeeName;

    @JsonProperty("employee_salary")
    private Integer employeeSalary;

    @JsonProperty("employee_age")
    private Integer employeeAge;

    @JsonProperty("employee_title")
    private String employeeTitle;

    @JsonProperty("employee_email")
    private String employeeEmail;

    public Employee toEmployee() {
        return Employee.builder()
                .id(id)
                .name(employeeName)
                .salary(employeeSalary)
                .age(employeeAge)
                .title(employeeTitle)
                .email(employeeEmail)
                .build();
    }
}
