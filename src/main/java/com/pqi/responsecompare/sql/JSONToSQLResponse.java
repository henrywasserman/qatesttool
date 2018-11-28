package com.pqi.responsecompare.sql;

        import com.fasterxml.jackson.core.JsonProcessingException;
        import com.fasterxml.jackson.databind.JsonNode;
        import com.fasterxml.jackson.databind.ObjectMapper;
        import com.pqi.responsecompare.configuration.PropertiesSingleton;
        import org.apache.log4j.Logger;

        import java.io.IOException;
        import java.util.*;

public enum JSONToSQLResponse {

    Instance;

    static Logger logger = Logger.getLogger(JSONToSQLResponse.class);

    private ArrayList<String> columns = null;
    private HashMap<String,ArrayList> rows = null;
    private String column = "";

    private JSONToSQLResponse() {
        Properties responseCompare = PropertiesSingleton.Instance.getProps();
        columns = new ArrayList<String>();
        rows = new HashMap<String,ArrayList>();
        //combineMaps(new HashMap<String,Object>((Map)responseCompare));
    }

    public ArrayList<String> getColumns() {
        return columns;
    }

    public HashMap<String,ArrayList> getRows() {
        return rows;
    }

    public void jsonToColumnsAndRows(String json)
            throws JsonProcessingException, IOException {
        columns = new ArrayList<String>();
        rows = new HashMap<String,ArrayList>();

        ObjectMapper mapper = new ObjectMapper();
        JsonNode jsonNode = mapper.readTree(json);
        walker(null, jsonNode);
        column = "";
    }

    private void walker(String nodename, JsonNode node) {
        String nameToPrint = nodename != null ? nodename : "must_be_root";

        if (node.isObject()) {
            Iterator<Map.Entry<String, JsonNode>> iterator = node.fields();
            ArrayList<Map.Entry<String, JsonNode>> nodesList = new ArrayList();

            while (iterator.hasNext()) {
                nodesList.add(iterator.next());
            }

            for (Map.Entry<String, JsonNode> nodEntry : nodesList) {
                column = nodEntry.getKey();
                columns.add(column);
                JsonNode newNode = nodEntry.getValue();

                walker(column, newNode);
            }
        } else if (node.isArray()) {
            Iterator<JsonNode> arrayItemsIterator = node.elements();
            ArrayList<JsonNode> arrayItemsList = new ArrayList();

            while (arrayItemsIterator.hasNext()) {
                arrayItemsList.add(arrayItemsIterator.next());
            }

            for (JsonNode arrayNode : arrayItemsList) {
                walker("array item", arrayNode);
            }
        } else {
            if (node.isValueNode()) {

                if (rows.get(column) == null ) {
                    rows.put(column, new ArrayList<String>());
                }

                rows.get(column).add(node.asText());

            } else {
                logger.info("  node is of some other type");
            }
        }
    }
}