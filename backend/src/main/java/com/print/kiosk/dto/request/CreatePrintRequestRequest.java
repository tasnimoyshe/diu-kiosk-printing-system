package com.print.kiosk.dto.request;

import com.print.kiosk.entity.PrintColor;
import com.print.kiosk.entity.PrintingSide;
import com.print.kiosk.entity.UserType;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public class CreatePrintRequestRequest {

    @NotBlank(message = "Full name is required")
    @Size(max = 40, message = "Full name must not exceed 40 characters")
    private String fullName;

    @NotBlank(message = "ID or registration number is required")
    @Pattern(
            regexp = "^\\d{3}-\\d{2}-\\d{1,6}$",
            message = "ID or registration number must follow the format 242-15-125"
    )
    private String registrationNumber;

    @NotNull(message = "User type is required")
    private UserType userType;

    @Size(max = 100, message = "Department must not exceed 100 characters")
    private String department;

    @NotNull(message = "Print color is required")
    private PrintColor printColor;

    @NotNull(message = "Printing side is required")
    private PrintingSide printingSide;

    @Min(value = 1, message = "Number of copies must be at least 1")
    @Max(value = 100, message = "Number of copies must not exceed 100")
    private int copies;

    public String getFullName() {
        return fullName;
    }

    public void setFullName(String fullName) {
        this.fullName = fullName;
    }

    public String getRegistrationNumber() {
        return registrationNumber;
    }

    public void setRegistrationNumber(String registrationNumber) {
        this.registrationNumber = registrationNumber;
    }

    public UserType getUserType() {
        return userType;
    }

    public void setUserType(UserType userType) {
        this.userType = userType;
    }

    public String getDepartment() {
        return department;
    }

    public void setDepartment(String department) {
        this.department = department;
    }

    public PrintColor getPrintColor() {
        return printColor;
    }

    public void setPrintColor(PrintColor printColor) {
        this.printColor = printColor;
    }

    public PrintingSide getPrintingSide() {
        return printingSide;
    }

    public void setPrintingSide(PrintingSide printingSide) {
        this.printingSide = printingSide;
    }

    public int getCopies() {
        return copies;
    }

    public void setCopies(int copies) {
        this.copies = copies;
    }
}