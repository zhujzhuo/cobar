package com.alibaba.cobar.config.loader;

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

import com.alibaba.cobar.config.ClusterConfig;
import com.alibaba.cobar.config.CobarConfig;
import com.alibaba.cobar.config.DataNodesConfig;
import com.alibaba.cobar.config.DataSourcesConfig;
import com.alibaba.cobar.config.InstancesConfig;
import com.alibaba.cobar.config.MachinesConfig;
import com.alibaba.cobar.config.QuarantineConfig;
import com.alibaba.cobar.config.SchemasConfig;
import com.alibaba.cobar.config.ServerConfig;
import com.alibaba.cobar.config.UsersConfig;

/**
 * @author xianmao.hexm
 */
public class XmlLoader {

    public CobarConfig load(String xml) throws ParserConfigurationException, SAXException, IOException,
            IntrospectionException, IllegalArgumentException, IllegalAccessException, InvocationTargetException {
        CobarConfig config = null;
        Element root = getRootElement(xml);
        if (root != null) {
            config = new CobarConfig();
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

    ServerConfig loadServer(Element root) throws IntrospectionException, IllegalArgumentException,
            IllegalAccessException, InvocationTargetException {
        Map<String, String> values = null;
        NodeList nodeList = root.getElementsByTagName("server");
        for (int i = 0; i < nodeList.getLength(); i++) {
            Node node = nodeList.item(i);
            if (node instanceof Element) {
                values = getNodeProperties(node);
                break;
            }
        }
        ServerConfig server = new ServerConfig();
        setBeanProperties(values, server);
        return server;
    }

    ClusterConfig loadCluster(Element root) throws IllegalArgumentException, IntrospectionException,
            IllegalAccessException, InvocationTargetException {
        List<ClusterConfig.Node> list = new ArrayList<ClusterConfig.Node>();
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
                            ClusterConfig.Node clusterNode = new ClusterConfig.Node();
                            setBeanProperties(values, clusterNode);
                            list.add(clusterNode);
                        }
                    }
                }
                break;
            }
        }
        ClusterConfig cluster = new ClusterConfig();
        cluster.setNodeList(list);
        return cluster;
    }

    QuarantineConfig loadQuarantine(Element root) throws IllegalArgumentException, IntrospectionException,
            IllegalAccessException, InvocationTargetException {
        List<QuarantineConfig.Host> list = new ArrayList<QuarantineConfig.Host>();
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
                            QuarantineConfig.Host host = new QuarantineConfig.Host();
                            setBeanProperties(values, host);
                            list.add(host);
                        }
                    }
                }
                break;
            }
        }
        QuarantineConfig quarantine = new QuarantineConfig();
        quarantine.setHostList(list);
        return quarantine;
    }

    UsersConfig loadUsers(Element root) throws IllegalArgumentException, IntrospectionException, IllegalAccessException,
            InvocationTargetException {
        List<UsersConfig.User> list = new ArrayList<UsersConfig.User>();
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
                            UsersConfig.User user = new UsersConfig.User();
                            setBeanProperties(values, user);
                            list.add(user);
                        }
                    }
                }
                break;
            }
        }
        UsersConfig users = new UsersConfig();
        users.setUserList(list);
        return users;
    }

    SchemasConfig loadSchemas(Element root) throws IllegalArgumentException, IntrospectionException,
            IllegalAccessException, InvocationTargetException {
        List<SchemasConfig.Schema> list = new ArrayList<SchemasConfig.Schema>();
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
                            SchemasConfig.Schema schema = new SchemasConfig.Schema();
                            setBeanProperties(values, schema);
                            list.add(schema);
                        }
                    }
                }
                break;
            }
        }
        SchemasConfig schemas = new SchemasConfig();
        schemas.setSchemaList(list);
        return schemas;
    }

    DataNodesConfig loadDataNodes(Element root) throws IllegalArgumentException, IntrospectionException,
            IllegalAccessException, InvocationTargetException {
        List<DataNodesConfig.DataNode> list = new ArrayList<DataNodesConfig.DataNode>();
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
                            DataNodesConfig.DataNode dataNode = new DataNodesConfig.DataNode();
                            setBeanProperties(values, dataNode);
                            list.add(dataNode);
                        }
                    }
                }
                break;
            }
        }
        DataNodesConfig dataNodes = new DataNodesConfig();
        dataNodes.setDataNodeList(list);
        return dataNodes;
    }

    DataSourcesConfig loadDataSources(Element root) throws IllegalArgumentException, IntrospectionException,
            IllegalAccessException, InvocationTargetException {
        List<DataSourcesConfig.DataSource> list = new ArrayList<DataSourcesConfig.DataSource>();
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
                            DataSourcesConfig.DataSource schema = new DataSourcesConfig.DataSource();
                            setBeanProperties(values, schema);
                            list.add(schema);
                        }
                    }
                }
                break;
            }
        }
        DataSourcesConfig scheams = new DataSourcesConfig();
        scheams.setDataSourceList(list);
        return scheams;
    }

    InstancesConfig loadInstances(Element root) throws IllegalArgumentException, IntrospectionException,
            IllegalAccessException, InvocationTargetException {
        List<InstancesConfig.Instance> list = new ArrayList<InstancesConfig.Instance>();
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
                            InstancesConfig.Instance instance = new InstancesConfig.Instance();
                            setBeanProperties(values, instance);
                            list.add(instance);
                        }
                    }
                }
                break;
            }
        }
        InstancesConfig instances = new InstancesConfig();
        instances.setInstanceList(list);
        return instances;
    }

    MachinesConfig loadMachines(Element root) throws IllegalArgumentException, IntrospectionException,
            IllegalAccessException, InvocationTargetException {
        List<MachinesConfig.Machine> list = new ArrayList<MachinesConfig.Machine>();
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
                            MachinesConfig.Machine machine = new MachinesConfig.Machine();
                            setBeanProperties(values, machine);
                            list.add(machine);
                        }
                    }
                }
                break;
            }
        }
        MachinesConfig machines = new MachinesConfig();
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
