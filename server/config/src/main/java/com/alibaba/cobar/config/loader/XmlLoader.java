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

import com.alibaba.cobar.config.CobarModel;
import com.alibaba.cobar.config.model.ClusterModel;
import com.alibaba.cobar.config.model.DataNodesModel;
import com.alibaba.cobar.config.model.DataSourcesModel;
import com.alibaba.cobar.config.model.InstancesModel;
import com.alibaba.cobar.config.model.MachinesModel;
import com.alibaba.cobar.config.model.QuarantineModel;
import com.alibaba.cobar.config.model.SchemasModel;
import com.alibaba.cobar.config.model.ServerModel;
import com.alibaba.cobar.config.model.UsersModel;

/**
 * @author xianmao.hexm
 */
public class XmlLoader {

    public CobarModel load(String xml) throws ParserConfigurationException, SAXException, IOException,
            IntrospectionException, IllegalArgumentException, IllegalAccessException, InvocationTargetException {
        CobarModel config = null;
        Element root = getRootElement(xml);
        if (root != null) {
            config = new CobarModel();
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

    ServerModel loadServer(Element root) throws IntrospectionException, IllegalArgumentException,
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
        ServerModel server = new ServerModel();
        setBeanProperties(values, server);
        return server;
    }

    ClusterModel loadCluster(Element root) throws IllegalArgumentException, IntrospectionException,
            IllegalAccessException, InvocationTargetException {
        List<ClusterModel.Node> list = new ArrayList<ClusterModel.Node>();
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
                            ClusterModel.Node clusterNode = new ClusterModel.Node();
                            setBeanProperties(values, clusterNode);
                            list.add(clusterNode);
                        }
                    }
                }
                break;
            }
        }
        ClusterModel cluster = new ClusterModel();
        cluster.setNodeList(list);
        return cluster;
    }

    QuarantineModel loadQuarantine(Element root) throws IllegalArgumentException, IntrospectionException,
            IllegalAccessException, InvocationTargetException {
        List<QuarantineModel.Host> list = new ArrayList<QuarantineModel.Host>();
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
                            QuarantineModel.Host host = new QuarantineModel.Host();
                            setBeanProperties(values, host);
                            list.add(host);
                        }
                    }
                }
                break;
            }
        }
        QuarantineModel quarantine = new QuarantineModel();
        quarantine.setHostList(list);
        return quarantine;
    }

    UsersModel loadUsers(Element root) throws IllegalArgumentException, IntrospectionException, IllegalAccessException,
            InvocationTargetException {
        List<UsersModel.User> list = new ArrayList<UsersModel.User>();
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
                            UsersModel.User user = new UsersModel.User();
                            setBeanProperties(values, user);
                            list.add(user);
                        }
                    }
                }
                break;
            }
        }
        UsersModel users = new UsersModel();
        users.setUserList(list);
        return users;
    }

    SchemasModel loadSchemas(Element root) throws IllegalArgumentException, IntrospectionException,
            IllegalAccessException, InvocationTargetException {
        List<SchemasModel.Schema> list = new ArrayList<SchemasModel.Schema>();
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
                            SchemasModel.Schema schema = new SchemasModel.Schema();
                            setBeanProperties(values, schema);
                            list.add(schema);
                        }
                    }
                }
                break;
            }
        }
        SchemasModel schemas = new SchemasModel();
        schemas.setSchemaList(list);
        return schemas;
    }

    DataNodesModel loadDataNodes(Element root) throws IllegalArgumentException, IntrospectionException,
            IllegalAccessException, InvocationTargetException {
        List<DataNodesModel.DataNode> list = new ArrayList<DataNodesModel.DataNode>();
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
                            DataNodesModel.DataNode dataNode = new DataNodesModel.DataNode();
                            setBeanProperties(values, dataNode);
                            list.add(dataNode);
                        }
                    }
                }
                break;
            }
        }
        DataNodesModel dataNodes = new DataNodesModel();
        dataNodes.setDataNodeList(list);
        return dataNodes;
    }

    DataSourcesModel loadDataSources(Element root) throws IllegalArgumentException, IntrospectionException,
            IllegalAccessException, InvocationTargetException {
        List<DataSourcesModel.DataSource> list = new ArrayList<DataSourcesModel.DataSource>();
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
                            DataSourcesModel.DataSource schema = new DataSourcesModel.DataSource();
                            setBeanProperties(values, schema);
                            list.add(schema);
                        }
                    }
                }
                break;
            }
        }
        DataSourcesModel scheams = new DataSourcesModel();
        scheams.setDataSourceList(list);
        return scheams;
    }

    InstancesModel loadInstances(Element root) throws IllegalArgumentException, IntrospectionException,
            IllegalAccessException, InvocationTargetException {
        List<InstancesModel.Instance> list = new ArrayList<InstancesModel.Instance>();
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
                            InstancesModel.Instance instance = new InstancesModel.Instance();
                            setBeanProperties(values, instance);
                            list.add(instance);
                        }
                    }
                }
                break;
            }
        }
        InstancesModel instances = new InstancesModel();
        instances.setInstanceList(list);
        return instances;
    }

    MachinesModel loadMachines(Element root) throws IllegalArgumentException, IntrospectionException,
            IllegalAccessException, InvocationTargetException {
        List<MachinesModel.Machine> list = new ArrayList<MachinesModel.Machine>();
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
                            MachinesModel.Machine machine = new MachinesModel.Machine();
                            setBeanProperties(values, machine);
                            list.add(machine);
                        }
                    }
                }
                break;
            }
        }
        MachinesModel machines = new MachinesModel();
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
