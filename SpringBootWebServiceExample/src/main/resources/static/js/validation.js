// Client-side validation for student forms
document.addEventListener('DOMContentLoaded', function() {
    const addStudentForm = document.getElementById('addStudentForm');
    if (addStudentForm) {
        addStudentForm.addEventListener('submit', function(event) {
            if (!validateStudentForm(this)) {
                event.preventDefault();
                return false;
            }
            return true;
        });
    }
    
    // Add real-time validation feedback
    const studentIdInput = document.getElementById('studentId');
    if (studentIdInput) {
        studentIdInput.addEventListener('input', function() {
            validateStudentIdRealTime(this);
        });
    }
    
    const firstNameInput = document.getElementById('firstName');
    if (firstNameInput) {
        firstNameInput.addEventListener('input', function() {
            validateNameRealTime(this, 'First name');
        });
    }
    
    const lastNameInput = document.getElementById('lastName');
    if (lastNameInput) {
        lastNameInput.addEventListener('input', function() {
            validateNameRealTime(this, 'Last name');
        });
    }
});

function validateStudentForm(form) {
    const studentId = form.studentId.value.trim();
    const firstName = form.firstName.value.trim();
    const lastName = form.lastName.value.trim();
    
    let isValid = true;
    let errorMessages = [];
    
    // Validate Student ID
    const studentIdError = validateStudentId(studentId);
    if (studentIdError) {
        isValid = false;
        errorMessages.push(studentIdError);
        highlightError(form.studentId);
    } else {
        clearError(form.studentId);
    }
    
    // Validate First Name
    const firstNameError = validateName(firstName, 'First name');
    if (firstNameError) {
        isValid = false;
        errorMessages.push(firstNameError);
        highlightError(form.firstName);
    } else {
        clearError(form.firstName);
    }
    
    // Validate Last Name
    const lastNameError = validateName(lastName, 'Last name');
    if (lastNameError) {
        isValid = false;
        errorMessages.push(lastNameError);
        highlightError(form.lastName);
    } else {
        clearError(form.lastName);
    }
    
    if (!isValid) {
        showValidationErrors(errorMessages);
    }
    
    return isValid;
}

function validateStudentId(studentId) {
    if (!studentId) {
        return "Student ID is required.";
    }
    
    if (studentId.length < 3 || studentId.length > 20) {
        return "Student ID must be between 3 and 20 characters.";
    }
    
    if (!/^[A-Za-z0-9]+$/.test(studentId)) {
        return "Student ID can only contain letters and numbers (no special characters or spaces).";
    }
    
    if (/\s/.test(studentId)) {
        return "Student ID cannot contain spaces.";
    }
    
    return null;
}

function validateName(name, fieldName) {
    if (!name) {
        return fieldName + " is required.";
    }
    
    if (name.length < 2 || name.length > 50) {
        return fieldName + " must be between 2 and 50 characters.";
    }
    
    if (!/^[A-Za-z\s.'-]+$/.test(name)) {
        return fieldName + " can only contain letters, spaces, apostrophes, periods, and hyphens.";
    }
    
    if (/^\s|\s$/.test(name)) {
        return fieldName + " cannot have leading or trailing spaces.";
    }
    
    if (/\s\s+/.test(name)) {
        return fieldName + " cannot have consecutive spaces.";
    }
    
    return null;
}

function validateStudentIdRealTime(input) {
    const error = validateStudentId(input.value.trim());
    if (error) {
        highlightError(input, error);
    } else {
        clearError(input);
    }
}

function validateNameRealTime(input, fieldName) {
    const error = validateName(input.value.trim(), fieldName);
    if (error) {
        highlightError(input, error);
    } else {
        clearError(input);
    }
}

function highlightError(input, message) {
    input.classList.add('is-invalid');
    input.classList.remove('is-valid');
    
    let feedback = input.nextElementSibling;
    if (!feedback || !feedback.classList.contains('invalid-feedback')) {
        feedback = document.createElement('div');
        feedback.className = 'invalid-feedback';
        input.parentNode.appendChild(feedback);
    }
    
    if (message) {
        feedback.textContent = message;
        feedback.style.display = 'block';
    }
}

function clearError(input) {
    input.classList.remove('is-invalid');
    input.classList.add('is-valid');
    
    const feedback = input.nextElementSibling;
    if (feedback && feedback.classList.contains('invalid-feedback')) {
        feedback.style.display = 'none';
    }
}

function showValidationErrors(messages) {
    // Remove any existing error alerts
    const existingAlerts = document.querySelectorAll('.validation-alert');
    existingAlerts.forEach(alert => alert.remove());
    
    if (messages.length > 0) {
        const alertDiv = document.createElement('div');
        alertDiv.className = 'alert alert-danger validation-alert';
        alertDiv.innerHTML = '<strong>Validation Errors:</strong><ul>' +
            messages.map(msg => `<li>${msg}</li>`).join('') +
            '</ul>';
        
        const form = document.querySelector('form');
        form.parentNode.insertBefore(alertDiv, form);
        
        // Auto-remove after 10 seconds
        setTimeout(() => {
            if (alertDiv.parentNode) {
                alertDiv.parentNode.removeChild(alertDiv);
            }
        }, 10000);
    }
}