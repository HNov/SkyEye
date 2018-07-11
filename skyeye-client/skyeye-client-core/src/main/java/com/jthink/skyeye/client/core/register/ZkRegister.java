package com.jthink.skyeye.client.core.register;

import com.jthink.skyeye.base.constant.Constants;
import com.jthink.skyeye.base.constant.RpcType;
import com.jthink.skyeye.client.core.constant.NodeMode;
import com.jthink.skyeye.client.core.util.SysUtil;
import com.jthink.skyeye.trace.dto.RegisterDto;
import com.jthink.skyeye.trace.generater.IncrementIdGen;
import com.jthink.skyeye.trace.registry.Registry;
import com.jthink.skyeye.trace.registry.ZookeeperRegistry;
import org.I0Itec.zkclient.ZkClient;

/**
 * JThink@JThink
 *
 * @author JThink
 * @version 0.0.1
 * @desc zookeeper注册中心
 * @date 2016-09-21 14:03:46
 */
public class ZkRegister {

    // zkClient
    private ZkClient client;

    public ZkRegister(ZkClient client) {
        this.client = client;
    }

    /**
     * 向注册中心注册节点信息
     * @param host
     * @param app
     * @param mail
     */
    public void registerNode(String host, String app, String mail) {
        // 注册永久节点用于历史日志查询
        this.create(Constants.SLASH + app + Constants.SLASH + host, NodeMode.PERSISTENT);
        this.getClient().writeData(Constants.ROOT_PATH_PERSISTENT + Constants.SLASH + app + Constants.SLASH + host,
                mail + Constants.SEMICOLON + SysUtil.userDir);
        // 注册临时节点用于日志滚屏
        this.getClient().createPersistent(Constants.ROOT_PATH_EPHEMERAL + Constants.SLASH + app, true);
        this.create(Constants.SLASH + app + Constants.SLASH + host, NodeMode.EPHEMERAL,
                Constants.APPENDER_INIT_DATA + Constants.SEMICOLON + SysUtil.userDir);
    }

    /**
     * rpc trace注册中心
     * @param host
     * @param app
     * @param rpc
     */
    public void registerRpc(String host, String app, String rpc) {
        if (rpc.equals(RpcType.dubbo.symbol())) {
            // TODO: 目前仅支持dubbo作为rpc/soa框架
            RegisterDto dto = new RegisterDto(app, host, this.client);
            Registry registry = new ZookeeperRegistry();
            IncrementIdGen.setId(registry.register(dto));
        }
    }

    /**
     * 创建节点
     * @param path
     * @param nodeMode
     */
    private void create(String path, NodeMode nodeMode) {
        if (nodeMode.symbol().equals(NodeMode.PERSISTENT.symbol())) {
            // 创建永久节点
            this.client.createPersistent(nodeMode.label() + path, true);
        } else if (nodeMode.symbol().equals(NodeMode.EPHEMERAL.symbol())) {
            // 创建临时节点
            this.client.createEphemeral(nodeMode.label() + path);
        }
    }

    /**
     * 创建带data的节点
     * @param path
     * @param nodeMode
     * @param data
     */
    private void create(String path, NodeMode nodeMode, String data) {
        if (nodeMode.symbol().equals(NodeMode.PERSISTENT.symbol())) {
            // 创建永久节点，加入数据
            this.client.createPersistent(nodeMode.label() + path, true);
        } else if (nodeMode.symbol().equals(NodeMode.EPHEMERAL.symbol())) {
            // 创建临时节点，加入数据
            this.client.createEphemeral(nodeMode.label() + path, data);
        }
    }

    /**
     * 写节点数据
     * @param path
     * @param nodeMode
     * @param data
     */
    public void write(String path, NodeMode nodeMode, String data) {
        if (nodeMode.symbol().equals(NodeMode.PERSISTENT.symbol())) {
            // 创建永久节点，加入数据
            this.client.writeData(nodeMode.label() + path, true);
        } else if (nodeMode.symbol().equals(NodeMode.EPHEMERAL.symbol())) {
            // 创建临时节点，加入数据
            this.client.writeData(nodeMode.label() + path, data);
        }
    }

    public ZkClient getClient() {
        return client;
    }

    public void setClient(ZkClient client) {
        this.client = client;
    }
}
