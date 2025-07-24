package org.openecomp.sdcrests.validation.rest;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/v1.0/validation")
@Validated
@Tag(name = "Validation", description = "Validation APIs")
public interface ValidationForSwaggerUsage {

    @PostMapping(
        value = "/{type}/validate",
        consumes = "multipart/form-data",
        produces = "application/json"
    )
    @Operation(description = "Validate a package")
    ResponseEntity validateFile(
        @PathVariable("type") String type,
        @RequestPart("validate") MultipartFile fileToValidate
    );
}
