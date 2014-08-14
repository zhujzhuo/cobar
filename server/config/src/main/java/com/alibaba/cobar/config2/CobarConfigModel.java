package com.alibaba.cobar.config2;

import java.beans.IntrospectionException;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;

import javax.xml.parsers.ParserConfigurationException;

import org.xml.sax.SAXException;

import com.alibaba.cobar.config2.loader.XmlLoader;
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
public class CobarConfigModel {

    private Server server;
    private Cluster cluster;
    private Quarantine quarantine;
    private Users users;
    private Schemas schemas;
    private DataNodes dataNodes;
    private DataSources dataSources;
    private Instances instances;
    private Machines machines;

    public static final CobarConfigModel getInstance() {
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

    /**
     * @return the server
     */
    public Server getServer() {
        return server;
    }

    /**
     * @param server the server to set
     */
    public void setServer(Server server) {
        this.server = server;
    }

    /**
     * @return the cluster
     */
    public Cluster getCluster() {
        return cluster;
    }

    /**
     * @param cluster the cluster to set
     */
    public void setCluster(Cluster cluster) {
        this.cluster = cluster;
    }

    /**
     * @return the quarantine
     */
    public Quarantine getQuarantine() {
        return quarantine;
    }

    /**
     * @param quarantine the quarantine to set
     */
    public void setQuarantine(Quarantine quarantine) {
        this.quarantine = quarantine;
    }

    /**
     * @return the users
     */
    public Users getUsers() {
        return users;
    }

    /**
     * @param users the users to set
     */
    public void setUsers(Users users) {
        this.users = users;
    }

    /**
     * @return the schemas
     */
    public Schemas getSchemas() {
        return schemas;
    }

    /**
     * @param schemas the schemas to set
     */
    public void setSchemas(Schemas schemas) {
        this.schemas = schemas;
    }

    /**
     * @return the dataNodes
     */
    public DataNodes getDataNodes() {
        return dataNodes;
    }

    /**
     * @param dataNodes the dataNodes to set
     */
    public void setDataNodes(DataNodes dataNodes) {
        this.dataNodes = dataNodes;
    }

    /**
     * @return the dataSources
     */
    public DataSources getDataSources() {
        return dataSources;
    }

    /**
     * @param dataSources the dataSources to set
     */
    public void setDataSources(DataSources dataSources) {
        this.dataSources = dataSources;
    }

    /**
     * @return the instances
     */
    public Instances getInstances() {
        return instances;
    }

    /**
     * @param instances the instances to set
     */
    public void setInstances(Instances instances) {
        this.instances = instances;
    }

    /**
     * @return the machines
     */
    public Machines getMachines() {
        return machines;
    }

    /**
     * @param machines the machines to set
     */
    public void setMachines(Machines machines) {
        this.machines = machines;
    }

}
