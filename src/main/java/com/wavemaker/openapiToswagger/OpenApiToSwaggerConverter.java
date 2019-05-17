package com.wavemaker.openapiToswagger;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.wavemaker.tools.apidocs.tools.core.model.*;
import com.wavemaker.tools.apidocs.tools.core.model.auth.ApiKeyAuthDefinition;
import com.wavemaker.tools.apidocs.tools.core.model.auth.In;
import com.wavemaker.tools.apidocs.tools.core.model.auth.OAuth2Definition;
import com.wavemaker.tools.apidocs.tools.core.model.auth.SecuritySchemeDefinition;
import com.wavemaker.tools.apidocs.tools.core.model.parameters.BodyParameter;
import com.wavemaker.tools.apidocs.tools.core.model.parameters.FormParameter;
import com.wavemaker.tools.apidocs.tools.core.model.parameters.HeaderParameter;
import com.wavemaker.tools.apidocs.tools.core.model.parameters.Parameter;
import com.wavemaker.tools.apidocs.tools.core.model.parameters.PathParameter;
import com.wavemaker.tools.apidocs.tools.core.model.parameters.QueryParameter;
import com.wavemaker.tools.apidocs.tools.core.model.properties.MapProperty;
import com.wavemaker.tools.apidocs.tools.core.model.properties.ObjectProperty;
import com.wavemaker.tools.apidocs.tools.core.model.properties.Property;
import com.wavemaker.tools.apidocs.tools.core.model.properties.RefProperty;
import io.swagger.v3.oas.models.ExternalDocumentation;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.PathItem;
import io.swagger.v3.oas.models.Paths;
import io.swagger.v3.oas.models.headers.Header;
import io.swagger.v3.oas.models.media.ArraySchema;
import io.swagger.v3.oas.models.media.Content;
import io.swagger.v3.oas.models.media.MediaType;
import io.swagger.v3.oas.models.media.Schema;
import io.swagger.v3.oas.models.media.StringSchema;
import io.swagger.v3.oas.models.parameters.RequestBody;
import io.swagger.v3.oas.models.responses.ApiResponse;
import io.swagger.v3.oas.models.responses.ApiResponses;
import io.swagger.v3.oas.models.security.OAuthFlows;
import io.swagger.v3.oas.models.security.SecurityScheme;
import io.swagger.v3.oas.models.servers.Server;

public class OpenApiToSwaggerConverter {

    private ObjectMapper mapper = new ObjectMapper();

    public Swagger convertToSwaggerv2(OpenAPI openAPI) {
        Swagger swagger = new Swagger();
        swagger.setInfo(infoBuilder(openAPI));
//        swagger.setBasePath(getBasePathFromOpenApi(openAPI.getServers()));
        swagger.setExternalDocs(externalDocBuilder(openAPI.getExternalDocs()));
        //Todo : security definations
//        swagger.setSecurityDefinitions(openAPI.getSecurity());
        swagger.setTags(tagBuilder(openAPI.getTags()));
        swagger.setPaths(pathBuilder(openAPI.getPaths()));
        swagger.setDefinitions(definationBuilder(openAPI.getComponents().getSchemas()));

        swagger.setSecurityDefinitions(securitySchemeBuilder(openAPI.getComponents().getSecuritySchemes()));

        List<Scheme> schemeList = new ArrayList<>();
        schemeList.add(Scheme.HTTP);
        schemeList.add(Scheme.HTTPS);
        swagger.setSchemes(schemeList);
        swagger.setHost(getHost(getBasePathFromOpenApi(openAPI.getServers())));
        swagger.setBasePath(getBasePath(getBasePathFromOpenApi(openAPI.getServers())));

//        swagger.setSecurityDefinitions(securitySchemeBuilder(openAPI.getComponents().));


        return swagger;

    }

    private String getBasePath(String completePath) {
        return completePath.substring(completePath.indexOf('/', 8));
    }

    private String getHost(String completePath) {
        if (completePath.startsWith("https://")) {
            return completePath.substring(8, completePath.indexOf('/', 8));
        } else if (completePath.startsWith("http://")) {
            return completePath.substring(7, completePath.indexOf('/', 7));
        }
        return "";
    }

    private Map<String, SecuritySchemeDefinition> securitySchemeBuilder(Map<String, SecurityScheme> securitySchemesMap) {
        if (securitySchemesMap != null) {
            Map<String, SecuritySchemeDefinition> securitySchemesV2 = new HashMap<>();
            for (Map.Entry<String, SecurityScheme> securitySchemeDefinitionEntry : securitySchemesMap.entrySet()) {
                securitySchemesV2.put(securitySchemeDefinitionEntry.getKey(),
                        buildSecuritySchemeDefinition(securitySchemeDefinitionEntry.getValue()));
            }
            return securitySchemesV2;
        }
        return null;
    }

    private SecuritySchemeDefinition buildSecuritySchemeDefinition(SecurityScheme securityScheme) {
        if (securityScheme.getType().toString().equals("oauth2")) {
            SecuritySchemeDefinition outh = new OAuth2Definition();
            if (securityScheme.getFlows().getImplicit() != null) {
                outh.setType(securityScheme.getType().toString());
                ((OAuth2Definition) outh).setFlow("implicit");
                ((OAuth2Definition) outh).setScopes(securityScheme.getFlows().getImplicit().getScopes());
                ((OAuth2Definition) outh).setAuthorizationUrl(securityScheme.getFlows().getImplicit().getAuthorizationUrl());
                return outh;
            } else if (securityScheme.getFlows().getAuthorizationCode() != null) {
                return null;
            } else if (securityScheme.getFlows().getClientCredentials() != null) {
                return null;
            }
        } else if (securityScheme.getType().toString().equals("apiKey")) {
            SecuritySchemeDefinition apikey = new ApiKeyAuthDefinition();
            apikey.setType(securityScheme.getType().toString());
            ((ApiKeyAuthDefinition) apikey).setIn(evaluateIn(securityScheme.getIn()));
            ((ApiKeyAuthDefinition) apikey).setName(securityScheme.getName());
            return apikey;
        }
        return null;
        //todo : write for one remaining.............
    }

    private In evaluateIn(SecurityScheme.In in) {
        if (in == SecurityScheme.In.HEADER) {
            return In.HEADER;
        } else if (in == SecurityScheme.In.QUERY) {
            return In.QUERY;
        }
        return null;
    }


    //todo : complete oauth flows.........
    private String checkOuthFlows(OAuthFlows oauthFlows) {
        if (oauthFlows.getImplicit() != null) {
            return "implicit";
        } else if (oauthFlows.getAuthorizationCode() != null) {
            return "";
        }
        return "";
    }

    private Map<String, Model> definationBuilder(Map<String, Schema> openApiSchemas) {

        Map<String, Model> definitionMap = new HashMap<>();

        for (Map.Entry<String, Schema> entry : openApiSchemas.entrySet()) {
            definitionMap.put(entry.getKey(), new ModelObjectBuilder().getModelFromSchema(entry.getValue()));
        }

        return definitionMap;
    }

    private Info infoBuilder(OpenAPI openAPI) {
        Info info = new Info();
        info.setTitle(openAPI.getInfo().getTitle());
//        info.setApiId(openAPI.getInfo().get);
        info.setDescription(openAPI.getInfo().getDescription());
        info.setContact(contactBuilder(openAPI.getInfo().getContact()));
        info.setVersion(openAPI.getInfo().getVersion());
        info.setTermsOfService(openAPI.getInfo().getTermsOfService());

//        info.setVendorExtension(openAPI.getInfo().getExtensions());
        info.setLicense(licenseBuilder(openAPI.getInfo().getLicense()));
        return info;
    }

    private Contact contactBuilder(io.swagger.v3.oas.models.info.Contact contact) {
        Contact contactv2 = new Contact();
        contactv2.setName(contact.getName());
        contactv2.setEmail(contact.getEmail());
        contactv2.setUrl(contact.getUrl());
        return contactv2;
    }

    private License licenseBuilder(io.swagger.v3.oas.models.info.License license) {
        License licensev2 = new License();
        licensev2.setName(license.getName());
        licensev2.setUrl(license.getUrl());
        return licensev2;
    }

    private ExternalDocs externalDocBuilder(ExternalDocumentation externalDocumentation) {
        ExternalDocs externalDocs = new ExternalDocs();
        if (externalDocumentation != null) {
            externalDocs.setDescription(externalDocumentation.getDescription());
            externalDocs.setUrl(externalDocumentation.getUrl());
            return externalDocs;
        }
        return null;
    }

    private List<Tag> tagBuilder(List<io.swagger.v3.oas.models.tags.Tag> openApiTags) {
        List<Tag> tags = new ArrayList<>();
        for (io.swagger.v3.oas.models.tags.Tag openApiTag : openApiTags) {
            Tag tag = new Tag();
            tag.setName(openApiTag.getName());
            tag.setDescription(openApiTag.getDescription());
            tag.setExternalDocs(externalDocBuilder(openApiTag.getExternalDocs()));
            //todo : extra fields in tags of swagger 2.0 :: controller names etc..
            tags.add(tag);
        }
        return tags;
    }

    private Map<String, Path> pathBuilder(Paths paths) {
        Map<String, Path> pathsv2 = new HashMap<>();
        for (Map.Entry<String, PathItem> pathItem : paths.entrySet()) {
            pathsv2.put(pathItem.getKey(), pathItemBuilder(pathItem.getValue()));
        }
        return pathsv2;
    }

    private Path pathItemBuilder(PathItem pathItem) {
        Path path = new Path();
//        path.setBasePath(pathItem.get);
        path.setGet(operationBuilder(pathItem.getGet()));
        path.setDelete(operationBuilder(pathItem.getDelete()));
        path.setOptions(operationBuilder(pathItem.getOptions()));
        path.setPatch(operationBuilder(pathItem.getPatch()));
        path.setPost(operationBuilder(pathItem.getPost()));
        path.setPut(operationBuilder(pathItem.getPut()));
        path.setParameters(parameterBuilder(pathItem.getParameters()));


//        path.setRelativePath(pathItemBuilder);
//        path.setTag(pathItem.getT);
//        path.setTarget
//        path.setCompletePath(pathItem);
//        path.setBasePath(pathItem.);
//        path.setParameters(pathItem.getParameters());
        return path;
    }

    private List<Parameter> parameterBuilder(List<io.swagger.v3.oas.models.parameters.Parameter> parameters) {
        List<Parameter> parameterListv2 = new ArrayList<>();
        if (parameters == null) {
            return null;
        } else {

            for (io.swagger.v3.oas.models.parameters.Parameter parameter : parameters) {
                if (parameter.getIn().equals("query")) {
                    Parameter queryparameter = new QueryParameter();
                    queryparameter.setName(parameter.getName());
                    if (parameter.getRequired() == null) {
                        queryparameter.setRequired(false);
                    } else {
                        queryparameter.setRequired(parameter.getRequired());
                    }
//                    ((QueryParameter) queryparameter).setCollectionFormat();
                    queryparameter.setDescription(parameter.getDescription());
                    queryparameter.setIn(parameter.getIn());
                    ((QueryParameter) queryparameter).setType(parameter.getSchema().getType());
                    ((QueryParameter) queryparameter).setFormat(parameter.getSchema().getFormat());
                    ((QueryParameter) queryparameter).setItems(itemSetter(parameter.getSchema()));
                    parameterListv2.add(queryparameter);
                } else if (parameter.getIn().equals("body")) {
                    Parameter bodyParameter = new BodyParameter();
                    bodyParameter.setName(parameter.getName());
                    bodyParameter.setRequired(parameter.getRequired());
                    bodyParameter.setDescription(parameter.getDescription());
                    bodyParameter.setIn(parameter.getIn());
//                    ((BodyParameter) bodyParameter).setSchema(parameter.getSchema());

//                    ((BodyParameter) bodyParameter).setType(parameter.getSchema().getType());
//                    ((BodyParameter) bodyParameter).setFormat(parameter.getSchema().getFormat());
//                    ((BodyParameter) bodyParameter).setItems(itemSetter(parameter.getSchema()));
                    parameterListv2.add(bodyParameter);
                } else if (parameter.getIn().equals("path")) {
                    Parameter pathParameter = new PathParameter();
                    pathParameter.setName(parameter.getName());
                    pathParameter.setRequired(parameter.getRequired());
                    pathParameter.setDescription(parameter.getDescription());
                    pathParameter.setIn(parameter.getIn());
                    ((PathParameter) pathParameter).setType(parameter.getSchema().getType());
                    ((PathParameter) pathParameter).setFormat(parameter.getSchema().getFormat());
                    ((PathParameter) pathParameter).setItems(itemSetter(parameter.getSchema()));
                    parameterListv2.add(pathParameter);
                } else if (parameter.getIn().equals("header")) {
                    Parameter headerParameter = new HeaderParameter();
                    headerParameter.setName(parameter.getName());
                    headerParameter.setRequired(parameter.getRequired());
                    headerParameter.setDescription(parameter.getDescription());
                    headerParameter.setIn(parameter.getIn());
                    ((HeaderParameter) headerParameter).setType(parameter.getSchema().getType());
                    ((HeaderParameter) headerParameter).setFormat(parameter.getSchema().getFormat());
                    ((HeaderParameter) headerParameter).setItems(itemSetter(parameter.getSchema()));
                    parameterListv2.add(headerParameter);
                } else if (parameter.getIn().equals("formData")) {
                    Parameter formData = new FormParameter();
                    formData.setName(parameter.getName());
                    formData.setDescription(parameter.getDescription());
                    formData.setRequired(parameter.getRequired());
                    ((FormParameter) formData).setType(parameter.getSchema().getType());
                    ((FormParameter) formData).setItems(itemSetter(parameter.getSchema()));
                    parameterListv2.add(formData);
                }
            }
            return parameterListv2;
        }

    }

    private Property itemSetter(Schema schema) {
        if (schema instanceof ArraySchema) {
            Property property = null;
            if (((ArraySchema) schema).getItems().getType() != null && schema.get$ref() == null) {
                property = new ObjectProperty();
                ((ObjectProperty) property).setType(((ArraySchema) schema).getItems().getType());
            } else if (((ArraySchema) schema).getItems().get$ref() != null) {
                property = new RefProperty();
                ((RefProperty) property).set$ref(((ArraySchema) schema).getItems().get$ref()
                        .replace("#/components/schemas/", ""));
            }
            return property;
        }
        return null;
    }

    private Operation operationBuilder(io.swagger.v3.oas.models.Operation operation) {
        if (operation != null) {
            Operation operationv2 = new Operation();

            operationv2.setDeprecated(operation.getDeprecated());
            operationv2.setDescription(operation.getDescription());
            //        operationv2.setextesio
            operationv2.setExternalDocs(externalDocBuilder(operation.getExternalDocs()));
            operationv2.setOperationId(operation.getOperationId());

            operationv2.setSecurity(securityBuilder(operation.getSecurity()));
            operationv2.setSummary(operation.getSummary());
            operationv2.setTags(operation.getTags());

            if (operation.getRequestBody() != null) {
                List<Parameter> parameterList = parameterBuilder(operation.getParameters());
                if (parameterList == null) {
                    parameterList = new ArrayList<>();
                }
                parameterList.add(requestBodyParamBuilder(operation.getRequestBody()));
                operationv2.setParameters(parameterList);
            } else {
                operationv2.setParameters(parameterBuilder(operation.getParameters()));
            }
            //todo : check for vendor extensions and extensions...
            //        operationv2.setVendorExtension(operation.getExtensions());


            //todo : requestbody , responses, servers........

            operationv2.setResponses(responseBuilder(operation.getResponses()));


            //todo : set produces and consumes
//            operationv2.setConsumes();

            operationv2.setProduces(buildProduces(operation.getResponses()));
            operationv2.setConsumes(buildConsumes(operation.getRequestBody()));
//
            return operationv2;
        }
        return null;
    }

    private List<String> buildConsumes(RequestBody requestBody) {
        if (requestBody != null) {
            Content content = requestBody.getContent();
            if (content != null) {
                return new ArrayList<>(content.keySet());
            }
        }
        return new ArrayList<>(Collections.singletonList("application/json"));
    }

    private List<String> buildProduces(ApiResponses responses) {
        if (responses != null) {
            Set<String> producerSet = new HashSet<>();
            for (Map.Entry<String, ApiResponse> responseEntry : responses.entrySet()) {
                Content content = responseEntry.getValue().getContent();
                if (content != null) {
                    producerSet.addAll(content.keySet());
                }
            }
            if (producerSet.size() == 0) {
                producerSet.add("application/json");
            }
            return new ArrayList<>(producerSet);
        }
        return new ArrayList<>(Collections.singletonList("application/json"));
    }

    private Parameter requestBodyParamBuilder(RequestBody requestBody) {
        Parameter bodyParameter = new BodyParameter();
        bodyParameter.setIn("body");
        Schema schema = getSchemaFromContent(requestBody);
        if (schema != null && schema.get$ref() != null) {
            ((BodyParameter) bodyParameter).setSchema(
                    new RefModel(schema.get$ref().replace("#/components/requestBodies/", "")));
        }
        bodyParameter.setDescription(requestBody.getDescription());
        bodyParameter.setName("body");

        if (requestBody.getRequired() != null) {
            bodyParameter.setRequired(requestBody.getRequired());
        }
//        bodyParameter.(getSchemaFromContent(requestBody.getContent()).get$ref());
        return bodyParameter;
    }

    private Schema getSchemaFromContent(RequestBody body) {
        if (body.getContent() != null) {
            Content content = body.getContent();
            for (Map.Entry<String, MediaType> schemaEntry : content.entrySet()) {
                return schemaEntry.getValue().getSchema();
            }
        } else if (body.get$ref() != null) {
            Schema schema = new StringSchema();
            schema.set$ref(body.get$ref());
            return schema;
        }
        return null;
    }

    private Map<String, Response> responseBuilder(Map<String, ApiResponse> responses) {
        Map<String, Response> responseMap = new HashMap<>();
        if (responses != null) {
            for (Map.Entry<String, ApiResponse> responseEntry : responses.entrySet()) {
                responseMap.put(responseEntry.getKey(), responseObjectBuilder(responseEntry.getValue()));
            }
            return responseMap;
        }
        return null;
    }

    private Response responseObjectBuilder(ApiResponse apiResponse) {
        Response response = new Response();
        response.setDescription(apiResponse.getDescription());

        response.setSchema(apiResponseContentAnalyzer(apiResponse.getContent()));
        response.setHeaders(headerBuilder(apiResponse.getHeaders()));



        return response;
    }

    private Map<String, Property> headerBuilder(Map<String, Header> headerMap) {
        if (headerMap != null) {
            Map<String, Property> headerPropertyV2 = new HashMap<>();
            for (Map.Entry<String, Header> headerEntry : headerMap.entrySet()) {
                headerPropertyV2.put(headerEntry.getKey(), getHeaderToProperty(headerEntry.getValue()));
            }
        }
        return null;
    }

    private Property getHeaderToProperty(Header header) {
        if (header != null) {
            Property property = new MapProperty();
            property.setDescription(header.getDescription());
            ((MapProperty) property).setFormat(header.getSchema().getFormat());
            ((MapProperty) property).setType(header.getSchema().getType());
            return property;
        }
        return null;
    }

    private Property apiResponseContentAnalyzer(Map<String, MediaType> contentMap) {
        if (contentMap != null) {
            for (Map.Entry<String, MediaType> type : contentMap.entrySet()) {
                return new ModelObjectBuilder().getproperty(type.getValue().getSchema());
            }
        }
        return null;
    }

    private List<Map<String, List<String>>> securityBuilder(List<io.swagger.v3.oas.models.security.SecurityRequirement> securityRequirements) {

        List<Map<String, List<String>>> securityList = new ArrayList<>();
        if (securityRequirements != null) {
            for (io.swagger.v3.oas.models.security.SecurityRequirement securityRequirementv3 : securityRequirements) {
                for (Map.Entry<String, List<String>> entry : securityRequirementv3.entrySet()) {
                    Map<String, List<String>> map = new HashMap<>();
                    map.put(entry.getKey(), entry.getValue());
                    securityList.add(map);
                }
            }
            return securityList;
        }
        return null;
    }

    private String getBasePathFromOpenApi(List<Server> servers) {
        for (Server server : servers) {
            return server.getUrl();
        }
        return "";
    }
}
