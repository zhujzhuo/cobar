package com.alibaba.cobar.config;

import java.beans.IntrospectionException;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;

import javax.xml.parsers.ParserConfigurationException;

import org.xml.sax.SAXException;

import com.alibaba.cobar.config.loader.XmlLoader;
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
public class CobarModel {

    private ServerModel server;
    private ClusterModel cluster;
    private QuarantineModel quarantine;
    private UsersModel users;
    private SchemasModel schemas;
    private DataNodesModel dataNodes;
    private DataSourcesModel dataSources;
    private InstancesModel instances;
    private MachinesModel machines;

    public static final CobarModel getInstance() {
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

    public ServerModel getServer() {
        return server;
    }

    public void setServer(ServerModel server) {
        this.server = server;
    }

    public ClusterModel getCluster() {
        return cluster;
    }

    public void setCluster(ClusterModel cluster) {
        this.cluster = cluster;
    }

    public QuarantineModel getQuarantine() {
        return quarantine;
    }

    public void setQuarantine(QuarantineModel quarantine) {
        this.quarantine = quarantine;
    }

    public UsersModel getUsers() {
        return users;
    }

    public void setUsers(UsersModel users) {
        this.users = users;
    }

    public SchemasModel getSchemas() {
        return schemas;
    }

    public void setSchemas(SchemasModel schemas) {
        this.schemas = schemas;
    }

    public DataNodesModel getDataNodes() {
        return dataNodes;
    }

    public void setDataNodes(DataNodesModel dataNodes) {
        this.dataNodes = dataNodes;
    }

    public DataSourcesModel getDataSources() {
        return dataSources;
    }

    public void setDataSources(DataSourcesModel dataSources) {
        this.dataSources = dataSources;
    }

    public InstancesModel getInstances() {
        return instances;
    }

    public void setInstances(InstancesModel instances) {
        this.instances = instances;
    }

    public MachinesModel getMachines() {
        return machines;
    }

    public void setMachines(MachinesModel machines) {
        this.machines = machines;
    }

}
