package org.edi3.jsonld;

import org.apache.commons.lang3.StringUtils;
import org.apache.poi.openxml4j.exceptions.InvalidFormatException;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.ss.usermodel.WorkbookFactory;

import javax.json.Json;
import javax.json.JsonArrayBuilder;
import javax.json.JsonObjectBuilder;
import java.io.*;
import java.util.*;
import org.edi3.*;

public class BSPToJSONLDVocabulary {

    public static void main(String[] args) throws IOException, InvalidFormatException {

        JsonObjectBuilder jsonObjectBuilder = Json.createObjectBuilder();
        JsonArrayBuilder graphJsonArrayBuilder = Json.createArrayBuilder();

        Workbook workbook = WorkbookFactory.create(new File("src/main/resources/BSP D20A Context CCL.xlsx"));
        Sheet sheet = workbook.getSheetAt(0);
        Iterator<Row> rowIterator = sheet.rowIterator();

        for (int i=0;i<5;i++){
            rowIterator.next();
        }
        Map<String, Entity> vocabulary = new HashMap<String, Entity>();
        Map<String, String> classMapping = new HashMap<String, String>();
        while (rowIterator.hasNext()){
            Row row = rowIterator.next();
            Entity entity = new Entity();
            entity.setId(getStringCellValue(row,1));
            entity.setType(getStringCellValue(row, 2));
            entity.setName(getStringCellValue(row, 3, false));
            entity.setDescription(getStringCellValue(row, 4, false));
            entity.setObjectClassTermQualifier(getStringCellValue(row, 7));
            entity.setObjectClassTerm(getStringCellValue(row, 8));
            entity.setPropertyTermQualifier(getStringCellValue(row, 9));
            entity.setPropertyTerm(getStringCellValue(row, 10));
            entity.setDataTypeQualifier(getStringCellValue(row, 11));
            entity.setRepresentationTerm(getStringCellValue(row, 12));
            entity.setQualifiedDataTypeId(getStringCellValue(row, 13));
            entity.setAssociatedObjectClassTermQualifier(getStringCellValue(row, 14));
            entity.setAssociatedObjectClassTerm(getStringCellValue(row, 15));
            entity.setBusinessTerm(getStringCellValue(row, 16));
            if (entity.getType() != null) {
                vocabulary.put(entity.getName(), entity);
            }
        }
        Map<String,Set<Entity>> classesMap = new HashMap<String, Set<Entity>>();
        Map<String,Set<Entity>> propertiesMap = new HashMap<String, Set<Entity>>();
        for(Entity entity:vocabulary.values()){
            if (entity.getType() == null){
                entity.getName();
            }
            else if (entity.getType().equalsIgnoreCase("ABIE")){
                Set<Entity> entities = new HashSet<Entity>();
                if(classesMap.containsKey(entity.getObjectClassTerm())){
                    entities = classesMap.get(entity.getObjectClassTerm());
                }
                entities.add(entity);
                classesMap.put(entity.getObjectClassTerm(), entities);
            }
            else if (entity.getType().equalsIgnoreCase("BBIE") || entity.getType().equalsIgnoreCase("ASBIE")){
                Set<Entity> entities = new HashSet<Entity>();
                if(propertiesMap.containsKey(entity.getBusinessTerm())){
                    entities = propertiesMap.get(entity.getBusinessTerm());
                }
                entities.add(entity);
                propertiesMap.put(entity.getBusinessTerm(), entities);
            }
        }
        Set<String> uniqueClasses = new HashSet<String>();

        for (String className:classesMap.keySet()){
            if(classesMap.get(className).size()==1){
                uniqueClasses.add(className);
            } else {
                System.err.println("Not unique className - " + className);
            }
        }
        for (String key:classesMap.keySet()){
            Set<Entity> entities = classesMap.get(key);
            Set<String> classTermQualifiers = new HashSet<String>();
            for (Entity entity:entities) {
                if (StringUtils.isNotEmpty(entity.getObjectClassTermQualifier())) {
                    classTermQualifiers.add(entity.getObjectClassTermQualifier());
                }
            }
            for (Entity entity:entities) {
                String id = key;
                if(entities.size()>1){
                    if(entity.getObjectClassTermQualifier().startsWith("Referenced")){
                        if(classTermQualifiers.contains(StringUtils.substringAfter(entity.getObjectClassTermQualifier(), "Referenced"))){
                            id = "Referenced".concat(key);
                        }
                    } else {
                        if(!classTermQualifiers.contains("Referenced".concat(entity.getObjectClassTermQualifier()))){
                            id = entity.getObjectClassTermQualifier().concat(key);
                        }
                    }                    
                }
                classMapping.put(entity.getClassTermWithQualfier(), id);
                JsonObjectBuilder rdfClass = Json.createObjectBuilder();
                rdfClass.add("@id", "edi3:".concat(id));
                rdfClass.add("@type", "rdfs:Class");
                if (entity.getDescription()!=null) {
                    rdfClass.add("rdfs:comment", entity.getDescription());
                }
                rdfClass.add("rdfs:label", entity.getName());
                rdfClass.add("edi3:cefactID", entity.getId());
                graphJsonArrayBuilder.add(rdfClass);
            }
        }
        if(classMapping.size()!=218){
            System.err.println("Unexpected number of ABIEs - " + classMapping.size());
        }
        Map<String,Set<Entity>> refinedPropertiesMap = new HashMap<String, Set<Entity>>();
        for (String key:propertiesMap.keySet()){
            Set<Entity> entities = propertiesMap.get(key);
            if (entities.size() == 1){
                refinedPropertiesMap.put(key, entities);
            } else {
                for (Entity entity:entities){
                    String newKey = classMapping.get(entity.getClassTermWithQualfier()).concat(key);
                    Set<Entity> newEntities = new HashSet<Entity>();
                    if(refinedPropertiesMap.containsKey(newKey)){
                        newEntities = refinedPropertiesMap.get(newKey);
                    }
                    newEntities.add(entity);
                    refinedPropertiesMap.put(newKey, newEntities);
                }
            }
        }

        Map<String,Set<Entity>> finalPropertiesMap = new HashMap<String, Set<Entity>>();
        for (String key:refinedPropertiesMap.keySet()){
            Set<Entity> entities = refinedPropertiesMap.get(key);
            if (entities.size() == 1){
                finalPropertiesMap.put(key, entities);
            } else {
                for (Entity entity:entities){
                    String newKey = entity.getObjectClassTermQualifier().concat(key);
                    Set<Entity> newEntities = new HashSet<Entity>();
                    if(finalPropertiesMap.containsKey(newKey)){
                        newEntities = finalPropertiesMap.get(newKey);
                    }
                    newEntities.add(entity);
                    finalPropertiesMap.put(newKey, newEntities);
                }
            }
        }
        if(finalPropertiesMap.size()!=2888){
            System.err.println("Unexpected number of ASBIEs/BBIEs - " + finalPropertiesMap.size());
        }
        for (String key:finalPropertiesMap.keySet()){
            Set<Entity> entities = finalPropertiesMap.get(key);
            for (Entity entity:entities) {
                String id = entities.size()==1?key:entity.getObjectClassTermQualifier().concat(key);
                JsonObjectBuilder rdfProperty = Json.createObjectBuilder();
                rdfProperty.add("@id", "edi3:".concat(id));
                rdfProperty.add("@type", "rdfs:Property");
                if (entity.getType().equalsIgnoreCase("BBIE")) {
                    //TODO: properly resolve data tyeps - qulaifiers, codes etc.
                    rdfProperty.add("rdfs:range", getData(entity.getRepresentationTerm()));
                }else {
                    String associatedKey =entity.getAssociatedClassTermWithQualifier();
                    String resolvedRange = classMapping.get(associatedKey);
                    if(resolvedRange == null){
                        resolvedRange = associatedKey;
                        System.out.println("Unresolved range - " + resolvedRange);
                    }
                    rdfProperty.add("rdfs:range", "edi3:".concat(resolvedRange));
                }
                if (entity.getDescription()!=null) {
                    rdfProperty.add("rdfs:comment", entity.getDescription());
                }
                String domain = classMapping.get(entity.getClassTermWithQualfier());
                if(domain == null) {
                    System.err.println("Can't resolve a domain for " + entity.getClassTermWithQualfier());
                }
                rdfProperty.add("rdfs:domain", "edi3:".concat(domain));
                rdfProperty.add("rdfs:label", entity.getName());
                rdfProperty.add("edi3:cefactID", entity.getId());
                graphJsonArrayBuilder.add(rdfProperty);
            }
        }
        jsonObjectBuilder.add("@graph", graphJsonArrayBuilder.build());
        PrintWriter writer = null;
        try {
            writer = new PrintWriter("bsp.json", "UTF-8");
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
        writer.print(jsonObjectBuilder.build());
        writer.close();
    }

    static String getStringCellValue(Row row, int cellNumber){
        return getStringCellValue(row, cellNumber, true);
    }
    static String getStringCellValue(Row row, int cellNumber, boolean cleanup){
        if(row.getCell(cellNumber)!=null){
            String result =row.getCell(cellNumber).getStringCellValue();
            if (cleanup){
                return cleanUp(result);
            }
            return StringUtils.defaultIfEmpty(result, "");
        }
        return "";
    }
    static String cleanUp(String attribute){
        return attribute.replaceAll(" ", "").replaceAll("_", "").replaceAll("-", "").replaceAll("/", "")/*.replaceAll(".","")*/;
    }

    static String getData(String dataType){
        try {
        UNType unType = UNType.valueOf(dataType.toUpperCase());
        switch (unType){
            case INDICATOR:
                return "xsd:boolean";
            case IDENTIFIER:
                return "xsd:token";//???
            case CODE:
                return "xsd:token";
            case TEXT:
                return "xsd:string";
            case DATETIME:
                return "xsd:dateTime";
            case AMOUNT:
                return "xsd:decimal";
            case PERCENT:
                return "xsd:decimal";
            case RATE:
                return "xsd:decimal";
            case DATE:
                return "xsd:date";
            case QUANTITY:
                return "xsd:decimal";
            case VALUE:
                return "xsd:string";
            case BINARYOBJECT:
                return "xsd:base64Binary";
            case NUMERIC:
                return "xsd:decimal";
            case MEASURE:
                return "xsd:decimal";
            case TYPE:
                return "xsd:string";
            case TIME:
                return "xsd:time";
            case GRAPHIC:
                return "xsd:base64Binary";  
            case PICTURE:
                return "xsd:base64Binary";
            case VIDEO:
                return "xsd:base64Binary";
            case SOUND:
                return "xsd:base64Binary";
        }

        } catch (IllegalArgumentException e){
            System.out.println(String.format("Check data type %s", dataType));
            return dataType;
        }
        return dataType;
    }

}
