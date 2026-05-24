package com.example.demo.config;

import com.example.demo.model.Permission;
import com.example.demo.model.Role;
import com.example.demo.repository.PermissionRepository;
import com.example.demo.repository.RoleRepository;
import com.example.demo.security.PermissionType;
import com.example.demo.security.RoleType;
import jakarta.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

@Component
public class DataInitializer {

    @Autowired
    private PermissionRepository permissionRepo;

    @Autowired
    private RoleRepository roleRepo;

    @PostConstruct
    public void init() {

        // ✅ ONE query — load all existing permissions
        Set<String> existingPermissions = permissionRepo.findAll()
                .stream()
                .map(Permission::getName)
                .collect(Collectors.toSet());

        // ✅ Insert only missing permissions
        for (PermissionType pt : PermissionType.values()) {
            if (!existingPermissions.contains(pt.name())) {
                Permission p = new Permission();
                p.setName(pt.name());
                permissionRepo.save(p);
            }
        }

        // ✅ ONE query — load all existing roles
        Set<String> existingRoles = roleRepo.findAll()
                .stream()
                .map(Role::getName)
                .collect(Collectors.toSet());

        // ✅ Insert only missing roles
        for (RoleType rt : RoleType.values()) {
            if (!existingRoles.contains(rt.name())) {
                Role r = new Role();
                r.setName(rt.name());
                roleRepo.save(r);
            }
        }

        // ✅ Reload as maps — O(1) lookup, zero extra DB calls in loops
        Map<String, Permission> permMap = permissionRepo.findAll()
                .stream()
                .collect(Collectors.toMap(Permission::getName, p -> p));

        Map<String, Role> roleMap = roleRepo.findAll()
                .stream()
                .collect(Collectors.toMap(Role::getName, r -> r));

        assignPermissions(roleMap, permMap, "HEAD_NURSE",
                "VIEW_PATIENT", "VIEW_REPORT", "DOWNLOAD_REPORT",
                "VIEW_VITALS", "MANAGE_SHIFT", "VIEW_MEDICINE_STOCK",
                "ENTER_VITALS", "UPDATE_PATIENT_STATUS",
                "ADD_NURSE_NOTES", "VIEW_DOCTOR_INSTRUCTION");

        assignPermissions(roleMap, permMap, "DEPARTMENT_NURSE",
                "ENTER_VITALS", "UPDATE_PATIENT_STATUS", "ADD_NURSE_NOTES",
                "VIEW_DOCTOR_INSTRUCTION", "VIEW_VITALS", "VIEW_PATIENT");

        assignPermissions(roleMap, permMap, "RECEPTIONIST",
                "CREATE_PATIENT", "BOOK_APPOINTMENT", "VIEW_PATIENT");

        assignPermissions(roleMap, permMap, "DOCTOR",
                "VIEW_VITALS", "UPDATE_DIAGNOSIS", "ADD_PRESCRIPTION",
                "DIGITAL_SIGN", "REQUEST_LAB", "APPROVE_DISCHARGE",
                "VIEW_LAB_RESULTS", "VIEW_PATIENT");

        assignPermissions(roleMap, permMap, "ADMIN",
                "CREATE_USER", "UPDATE_USER", "DELETE_USER",
                "VIEW_AUDIT_LOG", "VIEW_PATIENT", "VIEW_REPORT", "APPROVE_USER");
    }

    private void assignPermissions(Map<String, Role> roleMap,
                                   Map<String, Permission> permMap,
                                   String roleName,
                                   String... permissionNames) {

        Role role = roleMap.get(roleName);
        if (role == null) throw new RuntimeException("Role not found: " + roleName);

        Set<Permission> permissions = new HashSet<>();
        for (String name : permissionNames) {
            Permission p = permMap.get(name);
            if (p == null) throw new RuntimeException("Permission not found: " + name);
            permissions.add(p);
        }

        role.setPermissions(permissions);
        roleRepo.save(role);
    }
}