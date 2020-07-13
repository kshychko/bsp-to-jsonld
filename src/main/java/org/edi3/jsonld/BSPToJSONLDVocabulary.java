package org.edi3.jsonld;

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

            }
        }
        Map<String,Set<Entity>> refinedPropertiesMap = new HashMap<String, Set<Entity>>();
        for (String key:propertiesMap.keySet()){
            Set<Entity> entities = propertiesMap.get(key);
            if (entities.size() == 1){
                refinedPropertiesMap.put(key, entities);
            } else {
                for (Entity entity:entities){
                    String newKey = entity.objectClassTerm.concat(key);
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
                    String newKey = entity.objectClassTermQualifier.concat(key);
                    Set<Entity> newEntities = new HashSet<Entity>();
                    if(finalPropertiesMap.containsKey(newKey)){
                        newEntities = finalPropertiesMap.get(newKey);
                    }
                    newEntities.add(entity);
                    finalPropertiesMap.put(newKey, newEntities);
                }
            }
        }
        ;
        for (String key:classesMap.keySet()){
            Set<Entity> entities = classesMap.get(key);
            for (Entity entity:entities) {
                if (entity.objectClassTermQualifier == null){
                    entity.setObjectClassTermQualifier("");
                }
                String id = entities.size()==1?key:entity.getObjectClassTermQualifier().concat(key);
                JsonObjectBuilder rdfClass = Json.createObjectBuilder();
                rdfClass.add("@id", "bsp:".concat(id));
                rdfClass.add("@type", "rdfs:Class");
                if (entity.getDescription()!=null) {
                    rdfClass.add("rdfs:comment", entity.getDescription());
                }
                rdfClass.add("rdfs:label", entity.getName());
                graphJsonArrayBuilder.add(rdfClass);
            }
        }
        for (String key:finalPropertiesMap.keySet()){
            Set<Entity> entities = finalPropertiesMap.get(key);
            for (Entity entity:entities) {
                if (entity.objectClassTermQualifier == null){
                    entity.setObjectClassTermQualifier("");
                }
                String id = entities.size()==1?key:entity.getObjectClassTermQualifier().concat(key);
                JsonObjectBuilder rdfProperty = Json.createObjectBuilder();
                rdfProperty.add("@id", "bsp:".concat(id));
                rdfProperty.add("@type", "rdfs:Property");
                if (entity.getType().equalsIgnoreCase("BBIE")) {
                    rdfProperty.add("rdfs:range", "ccl:".concat(entity.getDataTypeQualifier().concat(entity.getRepresentationTerm())));
                }else {
                    rdfProperty.add("rdfs:range", "bsp:".concat(entity.getAssociatedObjectClassTerm()));
                }
                if (entity.getDescription()!=null) {
                    rdfProperty.add("rdfs:comment", entity.getDescription());
                }
                String domain = uniqueClasses.contains(entity.getObjectClassTerm())?entity.objectClassTerm:entity.objectClassTermQualifier.concat(entity.objectClassTerm);
                rdfProperty.add("rdfs:domain", "bsp:".concat(domain));
                rdfProperty.add("rdfs:label", entity.getName());
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
            return result;
        }
        return "";
    }
    static String cleanUp(String attribute){
        return attribute.replaceAll(" ", "").replaceAll("_", "").replaceAll("-", "").replaceAll("/", "")/*.replaceAll(".","")*/;
    }

}
