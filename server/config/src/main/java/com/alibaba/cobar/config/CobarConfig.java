package com.alibaba.cobar.config;

import java.beans.IntrospectionException;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;

import javax.xml.parsers.ParserConfigurationException;

import org.xml.sax.SAXException;

import com.alibaba.cobar.config.loader.XmlLoader;

/**
 * @author xianmao.hexm
 */
public class CobarConfig {

    private ServerConfig server;
    private ClusterConfig cluster;
    private QuarantineConfig quarantine;
    private UsersConfig users;
    private SchemasConfig schemas;
    private DataNodesConfig dataNodes;
    private DataSourcesConfig dataSources;
    private InstancesConfig instances;
    private MachinesConfig machines;

    public static final CobarConfig getInstance() {
        XmlLoader loader = new XmlLoader();
        try {
            return loader.load("/server.xml");
        } catch (IllegalArgumentException e) {
            throw new RuntimeException(e);
        } catch (ParserConfigurationException e) {
            throw new RuntimeException(e);
        } catch (SAXException e) {
            throw new RuntimeException(e);
        } catch (IOException e) {
            throw new RuntimeException(e);
        } catch (IntrospectionException e) {
            throw new RuntimeException(e);
        } catch (IllegalAccessException e) {
            throw new RuntimeException(e);
        } catch (InvocationTargetException e) {
            throw new RuntimeException(e);
        }
    }

    public ServerConfig getServer() {
        return server;
    }

    public void setServer(ServerConfig server) {
        this.server = server;
    }

    public ClusterConfig getCluster() {
        return cluster;
    }

    public void setCluster(ClusterConfig cluster) {
        this.cluster = cluster;
    }

    public QuarantineConfig getQuarantine() {
        return quarantine;
    }

    public void setQuarantine(QuarantineConfig quarantine) {
        this.quarantine = quarantine;
    }

    public UsersConfig getUsers() {
        return users;
    }

    public void setUsers(UsersConfig users) {
        this.users = users;
    }

    public SchemasConfig getSchemas() {
        return schemas;
    }

    public void setSchemas(SchemasConfig schemas) {
        this.schemas = schemas;
    }

    public DataNodesConfig getDataNodes() {
        return dataNodes;
    }

    public void setDataNodes(DataNodesConfig dataNodes) {
        this.dataNodes = dataNodes;
    }

    public DataSourcesConfig getDataSources() {
        return dataSources;
    }

    public void setDataSources(DataSourcesConfig dataSources) {
        this.dataSources = dataSources;
    }

    public InstancesConfig getInstances() {
        return instances;
    }

    public void setInstances(InstancesConfig instances) {
        this.instances = instances;
    }

    public MachinesConfig getMachines() {
        return machines;
    }

    public void setMachines(MachinesConfig machines) {
        this.machines = machines;
    }

}
