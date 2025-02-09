/*
 * Copyright (c) 2022 SAP SE or an SAP affiliate company and Eclipse Dirigible contributors
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v2.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v20.html
 *
 * SPDX-FileCopyrightText: 2022 SAP SE or an SAP affiliate company and Eclipse Dirigible contributors
 * SPDX-License-Identifier: EPL-2.0
 */
package org.eclipse.dirigible.components.security.endpoint;

import org.eclipse.dirigible.components.security.repository.RoleRepository;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import static org.eclipse.dirigible.components.security.repository.RoleRepositoryTest.createSecurityRole;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest(classes = {RoleRepository.class})
@AutoConfigureMockMvc
@ComponentScan(basePackages = {"org.eclipse.dirigible.components"})
@EntityScan("org.eclipse.dirigible.components")
@Transactional
class RoleEndpointTest {

    @Autowired
    private RoleRepository securityRoleRepository;

    @Autowired
    MockMvc mockMvc;

    @BeforeEach
    public void setup() {
    	
    	cleanup();
    	
        // Create test security roles
        securityRoleRepository.save(createSecurityRole("/a/b/c/test1.role", "test1", "description"));
        securityRoleRepository.save(createSecurityRole("/a/b/c/test2.role", "test2", "description"));
        securityRoleRepository.save(createSecurityRole("/a/b/c/test3.role", "test3", "description"));
        securityRoleRepository.save(createSecurityRole("/a/b/c/test4.role", "test4", "description"));
        securityRoleRepository.save(createSecurityRole("/a/b/c/test5.role", "test5", "description"));
    }

    @AfterEach
    public void cleanup() {
        // Delete test security roles
        securityRoleRepository.deleteAll();
    }

    @Test
    public void testGetSecurityRoles() throws Exception {
        mockMvc.perform(get("/services/v8/core/security/roles"))
                .andDo(print())
                .andExpect(status().isOk());
    }

    @SpringBootApplication
    static class TestConfiguration {
    }
}