package com.infodation.task_service.services;

import com.infodation.task_service.client.SpiceDBClient;
import com.infodation.task_service.components.JwtAuthenticationFilter;
import com.infodation.task_service.models.AssignPermissionRequest;
import com.infodation.task_service.models.CheckPermissionRequest;
import com.infodation.task_service.models.Folder;
import com.infodation.task_service.repositories.FolderRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.File;
import java.time.LocalDateTime;
import java.util.Date;

@Service
public class FolderService {
    @Autowired
    private FolderRepository folderRepository;
    @Autowired
    private SpiceDBClient client;

    public String createFolder(String path) {
        String[] pathArr = path.split("/");
        Folder parent = null;
        String currentPath = "";
        File folder;

        boolean existsFolder = folderRepository.existsByPath(path);

        if (existsFolder)
            return "Folder " + path + " has existed";


        AssignPermissionRequest assignObj = new AssignPermissionRequest(
                null,
                JwtAuthenticationFilter.USER_ID,
                "folder",
                "user",
                "owner"
        );

        for (String folderName : pathArr) {
            currentPath += "/" + folderName;

            Folder existingFolder = folderRepository.findByPath(currentPath).orElse(null);

            if (existingFolder != null) {
                parent = existingFolder;
                continue;
            }

            if (parent != null) {
                boolean hasWritePermission = client.checkPermission(
                        new CheckPermissionRequest(
                                parent.getId().toString(),
                                JwtAuthenticationFilter.USER_ID,
                                "folder",
                                "user",
                                "write"
                        )
                );

                if (!hasWritePermission) {
                    return "You don't have write permission in " + parent.getPath() + " folder";
                }
            }

            folder = new File(currentPath);
            if (!folder.exists() && !folder.mkdirs()) {
                return "Failed to create folder at: " + folder.getAbsolutePath();
            }

            Folder newFolder = new Folder();
            newFolder.setName(folderName);
            newFolder.setParentId(parent != null ? parent.getId() : null);
            newFolder.setCreatedAt(LocalDateTime.now());
            newFolder.setPath(currentPath);

            parent = folderRepository.save(newFolder);

            assignObj.setResourceId(parent.getId().toString());
            if (!client.assignPermissionAsBoolean(assignObj)) {
                return "Failed to assign ownership for " + currentPath;
            }
        }

        return "Folder created successfully: " + path;
    }


}
