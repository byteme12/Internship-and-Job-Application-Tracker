package com.usj.tracker.service;

import com.usj.tracker.domain.Company;
import com.usj.tracker.exception.CompanyNotFoundException;
import com.usj.tracker.repository.CompanyRepository;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

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
        if (companyRepository.findByNameIgnoreCase(company.getName().trim()).isPresent()) {
            throw new IllegalArgumentException("A company named \"" + company.getName().trim() + "\" already exists");
        }
        company.setName(company.getName().trim());
        return companyRepository.save(company);
    }

    public List<Company> getAllCompanies() {
        return companyRepository.findAll();
    }

    public Company getCompanyById(String id) {
        return companyRepository.findById(id)
                .orElseThrow(() -> new CompanyNotFoundException("Company not found with id: " + id));
    }

    public Company updateCompany(String id, Company update) {
        Company existing = getCompanyById(id);

        if (update.getName() == null || update.getName().isBlank()) {
            throw new IllegalArgumentException("Company name cannot be blank");
        }
        String newName = update.getName().trim();

        Optional<Company> nameClash = companyRepository.findByNameIgnoreCase(newName);
        if (nameClash.isPresent() && !nameClash.get().getId().equals(id)) {
            throw new IllegalArgumentException("A company named \"" + newName + "\" already exists");
        }

        existing.setName(newName);
        existing.setIndustry(update.getIndustry());
        existing.setLocation(update.getLocation());
        return companyRepository.save(existing);
    }

    public void deleteCompany(String id) {
        if (!companyRepository.existsById(id)) {
            throw new CompanyNotFoundException("Company not found with id: " + id);
        }
        companyRepository.deleteById(id);
    }
}
