package com.usj.tracker.service;

import com.usj.tracker.domain.Company;
import com.usj.tracker.exception.CompanyNotFoundException;
import com.usj.tracker.repository.CompanyRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class CompanyService {
    
    private final CompanyRepository companyRepository;

    public CompanyService(CompanyRepository companyRepository) {
        this.companyRepository = companyRepository;
    }

    public Company createCompany(Company company) {
        if (company.getName() == null || company.getName().isBlank()) {
            throw new IllegalArgumentException("Company name cannot be blank");
        }
        return companyRepository.save(company);
    }

    public List<Company> getAllCompanies() {
        return companyRepository.findAll();
    }

    public Company getCompanyById(String id) {
        return companyRepository.findById(id)
                .orElseThrow(() -> new CompanyNotFoundException("Company not found with id: " + id));
    }
}