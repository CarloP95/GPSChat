package com.carlop.coap
import org.eclipse.californium.core.CoapResource
import org.eclipse.californium.core.CoapServer
import org.eclipse.californium.core.network.config.NetworkConfig    

object InteractionManagerServer {

    private val DEFAULT_MAX_RESOURCE_SIZE = 1024 * 8
    private val DEFAULT_BLOCK_SIZE        = 1024

    def apply(resources : CoapResource*) : InteractionManagerServer = {
        val config = getDefaultConfig()
        new InteractionManagerServer(config, resources)
    }

    private def getDefaultConfig() : NetworkConfig = {
        val processors: Int         = Runtime.getRuntime().availableProcessors()
        var config : NetworkConfig  = NetworkConfig.getStandard()
        val defaultPort: Int        = config.getInt(Keys.COAP_PORT)
        val defaultSecurePort: Int  = config.getInt(Keys.COAP_SECURE_PORT)

        config
            .setInt(Keys.COAP_PORT, defaultPort + 100)
            .setInt(Keys.COAP_SECURE_PORT, defaultSecurePort + 100)
            .setInt(Keys.MAX_RESOURCE_BODY_SIZE, DEFAULT_MAX_RESOURCE_SIZE)
            .setInt(Keys.MAX_MESSAGE_SIZE, DEFAULT_BLOCK_SIZE)
            .setInt(Keys.PREFERRED_BLOCK_SIZE, DEFAULT_BLOCK_SIZE)
            .setInt(Keys.EXCHANGE_LIFETIME, 24700) // 24.7s instead of 247s
            .setInt(Keys.MAX_ACTIVE_PEERS, 200000)
            .setInt(Keys.DTLS_AUTO_RESUME_TIMEOUT, 0)
            .setInt(Keys.DTLS_CONNECTION_ID_LENGTH, 6)
            .setInt(Keys.MAX_PEER_INACTIVITY_PERIOD, 60 * 60 * 24) // 24h
            .setInt(Keys.TCP_CONNECTION_IDLE_TIMEOUT, 60 * 60 * 12) // 12h
            .setInt(Keys.TLS_HANDSHAKE_TIMEOUT, 60 * 1000) // 60s
            .setInt(Keys.SECURE_SESSION_TIMEOUT, 60 * 60 * 24) // 24h
            .setInt(Keys.HEALTH_STATUS_INTERVAL, 60) // 60s
            .setInt(Keys.UDP_CONNECTOR_RECEIVE_BUFFER, 0)
            .setInt(Keys.UDP_CONNECTOR_SEND_BUFFER, 0)
            .setInt(Keys.NETWORK_STAGE_RECEIVER_THREAD_COUNT, if( processors > 3) 2 else 1)
            .setInt(Keys.NETWORK_STAGE_SENDER_THREAD_COUNT, processors)
    }
}


class InteractionManagerServer(config: NetworkConfig, resources: Seq[CoapResource]) extends CoapServer (config: NetworkConfig) {
    resources.foreach( resource => this.add(resource)) // For now, adding the resources in a flat way.

    this.start()
}