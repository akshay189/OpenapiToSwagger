package com.wavemaker.openapi;

import java.io.File;
import java.io.IOException;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.wavemaker.openapiToswagger.OpenApiToSwaggerConverter;
import com.wavemaker.tools.apidocs.tools.core.model.Swagger;
import io.swagger.v3.oas.integration.IntegrationObjectMapperFactory;
import io.swagger.v3.oas.models.OpenAPI;

public class OpenApiModelConverter {


    public void sampleOpenApiModelConverter(String path) throws IOException {
        ObjectMapper mapper = IntegrationObjectMapperFactory.createJson();
        OpenAPI openApiObject = mapper.readValue(this.getClass().getResourceAsStream(path), OpenAPI.class);

        OpenApiToSwaggerConverter openApiToSwaggerConverter = new OpenApiToSwaggerConverter();
        Swagger swagger = openApiToSwaggerConverter.convertToSwaggerv2(openApiObject);

        File file = new File("target", "target.json");
        mapper.writerWithDefaultPrettyPrinter().writeValue(file, swagger);
        System.out.println(swagger);
    }


    public static void main(String[] args) {
        try {
            OpenApiModelConverter openApiModelConverter = new OpenApiModelConverter();
            openApiModelConverter.sampleOpenApiModelConverter("/openapi1.json");
        } catch (IOException e) {
            throw new RuntimeException();
        }
    }
}
