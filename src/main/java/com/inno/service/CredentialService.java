package com.inno.service;

import com.inno.repository.CredentialRepository;
import com.inno.repository.model.Credential;
import com.inno.service.exception.NotFoundException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import static com.inno.service.exception.ExceptionErrorCodes.CREDENTIAL_NOT_FOUND;
import static com.inno.service.exception.ExceptionErrorCodes.CREDENTIAL_NOT_FOUND_BY_USERNAME;

@Service
public class CredentialService implements UserDetailsService {
    private final CredentialRepository credentialRepository;

    @Autowired
    public CredentialService(CredentialRepository credentialRepository) {
        this.credentialRepository = credentialRepository;
    }

    @Override
    public Credential loadUserByUsername(String username) throws UsernameNotFoundException {
        return credentialRepository.findByUsername(username).orElseThrow(() ->
                new NotFoundException(String.format(CREDENTIAL_NOT_FOUND_BY_USERNAME.getErrorMessage(), username)));
    }

    public Credential findByUserId(Long userId) {
        return credentialRepository.findByUserId(userId).orElseThrow(() ->
                new NotFoundException(String.format(CREDENTIAL_NOT_FOUND.getErrorMessage(), userId)));
    }

    public boolean existsByUsername(String username) {
        return credentialRepository.existsByUsername(username);
    }

    public void save(Credential credential) {
        credentialRepository.save(credential);
    }
}
