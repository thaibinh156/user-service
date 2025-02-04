package com.infodation.task_service.controllers;

import com.authzed.api.v1.*;
import com.authzed.grpcutil.BearerToken;
import com.infodation.task_service.client.SpiceDBClient;
import com.infodation.task_service.models.AssignPermissionRequest;
import com.infodation.task_service.models.CheckPermissionRequest;
import io.grpc.ManagedChannel;
import io.grpc.ManagedChannelBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/spicedb")
public class SpiceDBController {

    private static final Logger log = LoggerFactory.getLogger(SpiceDBController.class);
    private final SpiceDBClient spiceDBClient;

    public SpiceDBController(SpiceDBClient spiceDBClient) {
        this.spiceDBClient = spiceDBClient;
    }

    @PostMapping("/update")
    public String updateSchema(@RequestBody String schema) {
        // Create schema service client
        return spiceDBClient.readSchema(schema);
    }


    @PostMapping("/assign-permission")
    public String assignPermission(@RequestBody AssignPermissionRequest requestObj) {
        return spiceDBClient.assignPermission(requestObj);
    }

    @PostMapping("/check-permission")
    public String checkPermission(@RequestBody CheckPermissionRequest requestObj) {
        return spiceDBClient.checkPermission(requestObj) ? "Permission granted" : "Permission denied";
    }

    @PostMapping("/delete-permission")
    public String deletePermission(@RequestBody AssignPermissionRequest requestObj) {
        return spiceDBClient.removePermission(requestObj);
    }
}
