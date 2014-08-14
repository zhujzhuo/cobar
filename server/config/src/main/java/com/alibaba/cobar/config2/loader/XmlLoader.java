package com.alibaba.cobar.config2.loader;

import java.beans.BeanInfo;
import java.beans.IntrospectionException;
import java.beans.Introspector;
import java.beans.PropertyDescriptor;
import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import com.alibaba.cobar.config2.CobarConfigModel;
import com.alibaba.cobar.config2.model.Cluster;
import com.alibaba.cobar.config2.model.DataNodes;
import com.alibaba.cobar.config2.model.DataSources;
import com.alibaba.cobar.config2.model.Instances;
import com.alibaba.cobar.config2.model.Machines;
import com.alibaba.cobar.config2.model.Quarantine;
import com.alibaba.cobar.config2.model.Schemas;
import com.alibaba.cobar.config2.model.Server;
import com.alibaba.cobar.config2.model.Users;

/**
 * @author xianmao.hexm
 */
public class XmlLoader {

    public CobarConfigModel load(String xml) throws ParserConfigurationException, SAXException, IOException,
            IntrospectionException, IllegalArgumentException, IllegalAccessException, InvocationTargetException {
        CobarConfigModel config = null;
        Element root = getRootElement(xml);
        if (root != null) {
            config = new CobarConfigModel();
            config.setServer(loadServer(root));
            config.setCluster(loadCluster(root));
            config.setQuarantine(loadQuarantine(root));
            config.setUsers(loadUsers(root));
            config.setSchemas(loadSchemas(root));
            config.setDataNodes(loadDataNodes(root));
            config.setDataSources(loadDataSources(root));
            config.setInstances(loadInstances(root));
            config.setMachines(loadMachines(root));
        }
        return config;
    }

    Server loadServer(Element root) throws IntrospectionException, IllegalArgumentException, IllegalAccessException,
            InvocationTargetException {
        Map<String, String> values = null;
        NodeList nodeList = root.getElementsByTagName("server");
        for (int i = 0; i < nodeList.getLength(); i++) {
            Node node = nodeList.item(i);
            if (node instanceof Element) {
                values = getNodeProperties(node);
                break;
            }
        }
        Server server = new Server();
        setBeanProperties(values, server);
        return server;
    }

    Cluster loadCluster(Element root) throws IllegalArgumentException, IntrospectionException, IllegalAccessException,
            InvocationTargetException {
        List<Cluster.Node> list = new ArrayList<Cluster.Node>();
        NodeList nodeList = root.getElementsByTagName("cluster");
        for (int i = 0; i < nodeList.getLength(); i++) {
            Node node = nodeList.item(i);
            if (node instanceof Element) {
                Element element = (Element) node;
                NodeList nl = element.getChildNodes();
                for (int j = 0; j < nl.getLength(); j++) {
                    Node n = nl.item(j);
                    if (n instanceof Element) {
                        Element e = (Element) n;
                        String name = e.getNodeName();
                        if ("node".equals(name)) {
                            Map<String, String> values = getNodeProperties(n);
                            Cluster.Node clusterNode = new Cluster.Node();
                            setBeanProperties(values, clusterNode);
                            list.add(clusterNode);
                        }
                    }
                }
                break;
            }
        }
        Cluster cluster = new Cluster();
        cluster.setNodeList(list);
        return cluster;
    }

    Quarantine loadQuarantine(Element root) throws IllegalArgumentException, IntrospectionException,
            IllegalAccessException, InvocationTargetException {
        List<Quarantine.Host> list = new ArrayList<Quarantine.Host>();
        NodeList nodeList = root.getElementsByTagName("quarantine");
        for (int i = 0; i < nodeList.getLength(); i++) {
            Node node = nodeList.item(i);
            if (node instanceof Element) {
                Element element = (Element) node;
                NodeList nl = element.getChildNodes();
                for (int j = 0; j < nl.getLength(); j++) {
                    Node n = nl.item(j);
                    if (n instanceof Element) {
                        Element e = (Element) n;
                        String name = e.getNodeName();
                        if ("host".equals(name)) {
                            Map<String, String> values = getNodeProperties(n);
                            Quarantine.Host host = new Quarantine.Host();
                            setBeanProperties(values, host);
                            list.add(host);
                        }
                    }
                }
                break;
            }
        }
        Quarantine quarantine = new Quarantine();
        quarantine.setHostList(list);
        return quarantine;
    }

    Users loadUsers(Element root) throws IllegalArgumentException, IntrospectionException, IllegalAccessException,
            InvocationTargetException {
        List<Users.User> list = new ArrayList<Users.User>();
        NodeList nodeList = root.getElementsByTagName("users");
        for (int i = 0; i < nodeList.getLength(); i++) {
            Node node = nodeList.item(i);
            if (node instanceof Element) {
                Element element = (Element) node;
                NodeList nl = element.getChildNodes();
                for (int j = 0; j < nl.getLength(); j++) {
                    Node n = nl.item(j);
                    if (n instanceof Element) {
                        Element e = (Element) n;
                        String name = e.getNodeName();
                        if ("user".equals(name)) {
                            Map<String, String> values = getNodeProperties(n);
                            Users.User user = new Users.User();
                            setBeanProperties(values, user);
                            list.add(user);
                        }
                    }
                }
                break;
            }
        }
        Users users = new Users();
        users.setUserList(list);
        return users;
    }

    Schemas loadSchemas(Element root) throws IllegalArgumentException, IntrospectionException, IllegalAccessException,
            InvocationTargetException {
        List<Schemas.Schema> list = new ArrayList<Schemas.Schema>();
        NodeList nodeList = root.getElementsByTagName("schemas");
        for (int i = 0; i < nodeList.getLength(); i++) {
            Node node = nodeList.item(i);
            if (node instanceof Element) {
                Element element = (Element) node;
                NodeList nl = element.getChildNodes();
                for (int j = 0; j < nl.getLength(); j++) {
                    Node n = nl.item(j);
                    if (n instanceof Element) {
                        Element e = (Element) n;
                        String name = e.getNodeName();
                        if ("schema".equals(name)) {
                            Map<String, String> values = getNodeProperties(n);
                            Schemas.Schema schema = new Schemas.Schema();
                            setBeanProperties(values, schema);
                            list.add(schema);
                        }
                    }
                }
                break;
            }
        }
        Schemas schemas = new Schemas();
        schemas.setSchemaList(list);
        return schemas;
    }

    DataNodes loadDataNodes(Element root) throws IllegalArgumentException, IntrospectionException,
            IllegalAccessException, InvocationTargetException {
        List<DataNodes.DataNode> list = new ArrayList<DataNodes.DataNode>();
        NodeList nodeList = root.getElementsByTagName("dataNodes");
        for (int i = 0; i < nodeList.getLength(); i++) {
            Node node = nodeList.item(i);
            if (node instanceof Element) {
                Element element = (Element) node;
                NodeList nl = element.getChildNodes();
                for (int j = 0; j < nl.getLength(); j++) {
                    Node n = nl.item(j);
                    if (n instanceof Element) {
                        Element e = (Element) n;
                        String name = e.getNodeName();
                        if ("dataNode".equals(name)) {
                            Map<String, String> values = getNodeProperties(n);
                            DataNodes.DataNode dataNode = new DataNodes.DataNode();
                            setBeanProperties(values, dataNode);
                            list.add(dataNode);
                        }
                    }
                }
                break;
            }
        }
        DataNodes dataNodes = new DataNodes();
        dataNodes.setDataNodeList(list);
        return dataNodes;
    }

    DataSources loadDataSources(Element root) throws IllegalArgumentException, IntrospectionException,
            IllegalAccessException, InvocationTargetException {
        List<DataSources.DataSource> list = new ArrayList<DataSources.DataSource>();
        NodeList nodeList = root.getElementsByTagName("dataSources");
        for (int i = 0; i < nodeList.getLength(); i++) {
            Node node = nodeList.item(i);
            if (node instanceof Element) {
                Element element = (Element) node;
                NodeList nl = element.getChildNodes();
                for (int j = 0; j < nl.getLength(); j++) {
                    Node n = nl.item(j);
                    if (n instanceof Element) {
                        Element e = (Element) n;
                        String name = e.getNodeName();
                        if ("dataSource".equals(name)) {
                            Map<String, String> values = getNodeProperties(n);
                            DataSources.DataSource schema = new DataSources.DataSource();
                            setBeanProperties(values, schema);
                            list.add(schema);
                        }
                    }
                }
                break;
            }
        }
        DataSources scheams = new DataSources();
        scheams.setDataSourceList(list);
        return scheams;
    }

    Instances loadInstances(Element root) throws IllegalArgumentException, IntrospectionException,
            IllegalAccessException, InvocationTargetException {
        List<Instances.Instance> list = new ArrayList<Instances.Instance>();
        NodeList nodeList = root.getElementsByTagName("instances");
        for (int i = 0; i < nodeList.getLength(); i++) {
            Node node = nodeList.item(i);
            if (node instanceof Element) {
                Element element = (Element) node;
                NodeList nl = element.getChildNodes();
                for (int j = 0; j < nl.getLength(); j++) {
                    Node n = nl.item(j);
                    if (n instanceof Element) {
                        Element e = (Element) n;
                        String name = e.getNodeName();
                        if ("instance".equals(name)) {
                            Map<String, String> values = getNodeProperties(n);
                            Instances.Instance instance = new Instances.Instance();
                            setBeanProperties(values, instance);
                            list.add(instance);
                        }
                    }
                }
                break;
            }
        }
        Instances instances = new Instances();
        instances.setInstanceList(list);
        return instances;
    }

    Machines loadMachines(Element root) throws IllegalArgumentException, IntrospectionException,
            IllegalAccessException, InvocationTargetException {
        List<Machines.Machine> list = new ArrayList<Machines.Machine>();
        NodeList nodeList = root.getElementsByTagName("machines");
        for (int i = 0; i < nodeList.getLength(); i++) {
            Node node = nodeList.item(i);
            if (node instanceof Element) {
                Element element = (Element) node;
                NodeList nl = element.getChildNodes();
                for (int j = 0; j < nl.getLength(); j++) {
                    Node n = nl.item(j);
                    if (n instanceof Element) {
                        Element e = (Element) n;
                        String name = e.getNodeName();
                        if ("machine".equals(name)) {
                            Map<String, String> values = getNodeProperties(n);
                            Machines.Machine machine = new Machines.Machine();
                            setBeanProperties(values, machine);
                            list.add(machine);
                        }
                    }
                }
                break;
            }
        }
        Machines machines = new Machines();
        machines.setMachineList(list);
        return machines;
    }

    private Element getRootElement(String xml) throws ParserConfigurationException, SAXException, IOException {
        Element root = null;
        InputStream is = null;
        try {
            is = XmlLoader.class.getResourceAsStream(xml);
            DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
            DocumentBuilder db = factory.newDocumentBuilder();
            Document doc = db.parse(is);
            root = doc.getDocumentElement();
        } finally {
            if (is != null) {
                is.close();
            }
        }
        return root;
    }

    private Map<String, String> getNodeProperties(Node node) {
        Element element = (Element) node;
        Map<String, String> values = new HashMap<String, String>();
        NodeList nl = element.getChildNodes();
        for (int j = 0; j < nl.getLength(); j++) {
            Node n = nl.item(j);
            if (n instanceof Element) {
                Element e = (Element) n;
                String name = e.getNodeName();
                if ("property".equals(name)) {
                    String key = e.getAttribute("name");
                    String value = e.getTextContent();
                    if (value != null) {
                        value = value.trim();
                    }
                    values.put(key, value);
                }
            }
        }
        return values;
    }

    private void setBeanProperties(Map<String, String> values, Object bean) throws IntrospectionException,
            IllegalArgumentException, IllegalAccessException, InvocationTargetException {
        BeanInfo beanInfo = Introspector.getBeanInfo(bean.getClass());
        PropertyDescriptor[] pds = beanInfo.getPropertyDescriptors();
        for (PropertyDescriptor pd : pds) {
            String name = pd.getName();
            String value = values.get(name);
            if (value != null) {
                Method method = pd.getWriteMethod();
                method.invoke(bean, value);
            }
        }
    }

}
