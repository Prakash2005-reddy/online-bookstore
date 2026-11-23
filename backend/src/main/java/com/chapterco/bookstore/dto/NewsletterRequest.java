package com.chapterco.bookstore.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record NewsletterRequest(@NotBlank @Email @Size(max = 190) String email) {}
