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

public class CCLToJSONLDVocabulary {

    public static void main(String[] args) throws IOException, InvalidFormatException {

        JsonObjectBuilder jsonObjectBuilder = Json.createObjectBuilder();
        JsonArrayBuilder graphJsonArrayBuilder = Json.createArrayBuilder();

        Workbook workbook = WorkbookFactory.create(new File("src/main/resources/CCL 20A_.xls"));
        Sheet sheet = workbook.getSheetAt(4);
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
            if (entity.getId() != null) {
                vocabulary.put(entity.getId(), entity);
            }
        }
        Set<Entity> uniqueClasses = new HashSet<Entity>();
        for(Entity entity:vocabulary.values()){
            if (entity.getType() == null){
                entity.getName();
            }
            else if (entity.getType().equalsIgnoreCase("DT")){
                uniqueClasses.add(entity);
            }
        }

        sheet = workbook.getSheetAt(7);
        rowIterator = sheet.rowIterator();

        for (int i=0;i<5;i++){
            rowIterator.next();
        }
        vocabulary = new HashMap<String, Entity>();
        while (rowIterator.hasNext()){
            Row row = rowIterator.next();
            Entity entity = new Entity();
            entity.setId(getStringCellValue(row,1));
            entity.setType(getStringCellValue(row, 2));
            entity.setName(getStringCellValue(row, 3, false));
            entity.setRepresentationTerm(getStringCellValue(row, 12));
            if (entity.getType().equalsIgnoreCase("DT")){
                row = rowIterator.next();
            }
            entity.setDescription(getStringCellValue(row, 4, false));
            if (entity.getId() != null) {
                vocabulary.put(entity.getId(), entity);
            }
        }
        for(Entity entity:vocabulary.values()){
            if (entity.getType() == null){
                entity.getName();
            }
            else if (entity.getType().equalsIgnoreCase("DT")){
                uniqueClasses.add(entity);
            }
        }

        for (Entity entity:uniqueClasses) {
            if (entity.objectClassTermQualifier == null){
                entity.setObjectClassTermQualifier("");
            }
            String id = entity.getRepresentationTerm();
            if(entity.getDataTypeQualifier()!=null){
                id=entity.getDataTypeQualifier().concat(id);
            }
            JsonObjectBuilder rdfClass = Json.createObjectBuilder();
            rdfClass.add("@id", "ccl:".concat(id));
            rdfClass.add("@type", "rdfs:Class");
            if (entity.getDescription()!=null) {
                rdfClass.add("rdfs:comment", entity.getDescription());
            }
            rdfClass.add("rdfs:label", entity.getName());
            graphJsonArrayBuilder.add(rdfClass);
        }
        jsonObjectBuilder.add("@graph", graphJsonArrayBuilder.build());
        PrintWriter writer = null;
        try {
            writer = new PrintWriter("ccl.json", "UTF-8");
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
        try {
            if (row.getCell(cellNumber) != null) {
                String result = row.getCell(cellNumber).getStringCellValue();
                if (cleanup) {
                    return cleanUp(result);
                }
                return result;
            }
        }catch (IllegalStateException e){
            /*e.printStackTrace();*/
        }
        return "";
    }
    static String cleanUp(String attribute){
        return attribute.replaceAll(" ", "").replaceAll("_", "").replaceAll("-", "").replaceAll("/", "")/*.replaceAll(".","")*/;
    }

}
