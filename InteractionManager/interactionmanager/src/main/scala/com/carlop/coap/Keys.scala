package com.carlop.coap

object Keys {

  /**
    * The maximum number of active peers supported.
    * <p>
    * An active peer is a node with which we exchange CoAP messages. For
    * each active peer we need to maintain some state, e.g. we need to keep
    * track of MIDs and tokens in use with the peer. It therefore is
    * reasonable to limit the number of peers so that memory consumption
    * can be better predicted.
    * <p>
    * The default value of this property is
    * {@link NetworkConfigDefaults#DEFAULT_MAX_ACTIVE_PEERS}.
    * <p>
    * For clients this value can safely be set to a small one or two digit
    * number as most clients will only communicate with a small set of
    * peers (servers).
    */ /**
    * The maximum number of active peers supported.
    * <p>
    * An active peer is a node with which we exchange CoAP messages. For
    * each active peer we need to maintain some state, e.g. we need to keep
    * track of MIDs and tokens in use with the peer. It therefore is
    * reasonable to limit the number of peers so that memory consumption
    * can be better predicted.
    * <p>
    * The default value of this property is
    * {@link NetworkConfigDefaults#DEFAULT_MAX_ACTIVE_PEERS}.
    * <p>
    * For clients this value can safely be set to a small one or two digit
    * number as most clients will only communicate with a small set of
    * peers (servers).
    */
  val MAX_ACTIVE_PEERS: String = "MAX_ACTIVE_PEERS"

  /**
    * The maximum number of seconds a peer may be inactive for before it is
    * considered stale and all state associated with it can be discarded.
    */ /**
    * The maximum number of seconds a peer may be inactive for before it is
    * considered stale and all state associated with it can be discarded.
    */
  val MAX_PEER_INACTIVITY_PERIOD: String = "MAX_PEER_INACTIVITY_PERIOD"

  val COAP_PORT: String = "COAP_PORT"

  val COAP_SECURE_PORT: String = "COAP_SECURE_PORT"

  val ACK_TIMEOUT: String = "ACK_TIMEOUT"

  val ACK_RANDOM_FACTOR: String = "ACK_RANDOM_FACTOR"

  val ACK_TIMEOUT_SCALE: String = "ACK_TIMEOUT_SCALE"

  val MAX_RETRANSMIT: String = "MAX_RETRANSMIT"

  /**
    * The EXCHANGE_LIFETIME as defined by the CoAP spec in MILLISECONDS.
    */ /**
    * The EXCHANGE_LIFETIME as defined by the CoAP spec in MILLISECONDS.
    */
  val EXCHANGE_LIFETIME: String = "EXCHANGE_LIFETIME"

  val NON_LIFETIME: String = "NON_LIFETIME"

  val MAX_TRANSMIT_WAIT: String = "MAX_TRANSMIT_WAIT"

  val MAX_LATENCY: String = "MAX_LATENCY"

  val MAX_SERVER_RESPONSE_DELAY: String = "MAX_SERVER_RESPONSE_DELAY"

  val NSTART: String = "NSTART"

  val LEISURE: String = "LEISURE"

  val PROBING_RATE: String = "PROBING_RATE"

  val USE_RANDOM_MID_START: String = "USE_RANDOM_MID_START"

  val MID_TRACKER: String = "MID_TACKER"

  val MID_TRACKER_GROUPS: String = "MID_TRACKER_GROUPS"

  /**
    * Base MID for multicast MID range. All multicast requests use the same
    * MID provider, which generates MIDs in the range [base...65536).
    * None multicast request use the range [0...base).
    * 0 := disable multicast support.
    */ /**
    * Base MID for multicast MID range. All multicast requests use the same
    * MID provider, which generates MIDs in the range [base...65536).
    * None multicast request use the range [0...base).
    * 0 := disable multicast support.
    */
  val MULTICAST_BASE_MID: String = "MULTICAST_BASE_MID"

  val TOKEN_SIZE_LIMIT: String = "TOKEN_SIZE_LIMIT"

  /**
    * The block size (number of bytes) to use when doing a blockwise
    * transfer. This value serves as the upper limit for block size in
    * blockwise transfers.
    */ /**
    * The block size (number of bytes) to use when doing a blockwise
    * transfer. This value serves as the upper limit for block size in
    * blockwise transfers.
    */
  val PREFERRED_BLOCK_SIZE: String = "PREFERRED_BLOCK_SIZE"

  /**
    * The maximum payload size (in bytes) that can be transferred in a
    * single message, i.e. without requiring a blockwise transfer.
    *
    * NB: this value MUST be adapted to the maximum message size supported
    * by the transport layer. In particular, this value cannot exceed the
    * network's MTU if UDP is used as the transport protocol.
    */ /**
    * The maximum payload size (in bytes) that can be transferred in a
    * single message, i.e. without requiring a blockwise transfer.
    *
    * NB: this value MUST be adapted to the maximum message size supported
    * by the transport layer. In particular, this value cannot exceed the
    * network's MTU if UDP is used as the transport protocol.
    */
  val MAX_MESSAGE_SIZE: String = "MAX_MESSAGE_SIZE"

  /**
    * The maximum size of a resource body (in bytes) that will be accepted
    * as the payload of a POST/PUT or the response to a GET request in a
    * <em>transparent</em> blockwise transfer.
    * <p>
    * This option serves as a safeguard against excessive memory
    * consumption when many resources contain large bodies that cannot be
    * transferred in a single CoAP message. This option has no impact on
    * *manually* managed blockwise transfers in which the blocks are
    * handled individually.
    * <p>
    * Note that this option does not prevent local clients or resource
    * implementations from sending large bodies as part of a request or
    * response to a peer.
    * <p>
    * The default value of this property is
    * {@link NetworkConfigDefaults#DEFAULT_MAX_RESOURCE_BODY_SIZE}.
    * <p>
    * A value of {@code 0} turns off transparent handling of blockwise
    * transfers altogether.
    */ /**
    * The maximum size of a resource body (in bytes) that will be accepted
    * as the payload of a POST/PUT or the response to a GET request in a
    * <em>transparent</em> blockwise transfer.
    * <p>
    * This option serves as a safeguard against excessive memory
    * consumption when many resources contain large bodies that cannot be
    * transferred in a single CoAP message. This option has no impact on
    * *manually* managed blockwise transfers in which the blocks are
    * handled individually.
    * <p>
    * Note that this option does not prevent local clients or resource
    * implementations from sending large bodies as part of a request or
    * response to a peer.
    * <p>
    * The default value of this property is
    * {@link NetworkConfigDefaults#DEFAULT_MAX_RESOURCE_BODY_SIZE}.
    * <p>
    * A value of {@code 0} turns off transparent handling of blockwise
    * transfers altogether.
    */
  val MAX_RESOURCE_BODY_SIZE: String = "MAX_RESOURCE_BODY_SIZE"

  /**
    * The maximum amount of time (in milliseconds) allowed between
    * transfers of individual blocks in a blockwise transfer before the
    * blockwise transfer state is discarded.
    * <p>
    * The default value of this property is
    * {@link NetworkConfigDefaults#DEFAULT_BLOCKWISE_STATUS_LIFETIME}.
    */ /**
    * The maximum amount of time (in milliseconds) allowed between
    * transfers of individual blocks in a blockwise transfer before the
    * blockwise transfer state is discarded.
    * <p>
    * The default value of this property is
    * {@link NetworkConfigDefaults#DEFAULT_BLOCKWISE_STATUS_LIFETIME}.
    */
  val BLOCKWISE_STATUS_LIFETIME: String = "BLOCKWISE_STATUS_LIFETIME"

  /**
    * Property to indicate if the response should always include the Block2 option when client request early blockwise negociation but the response can be sent on one packet.
    * <p>
    * The default value of this property is
    * {@link NetworkConfigDefaults#DEFAULT_BLOCKWISE_STRICT_BLOCK2_OPTION}.
    * <p>
    * A value of {@code false} indicate that the server will respond without block2 option if no further blocks are required.<br/>
    * A value of {@code true} indicate that the server will response with block2 option event if no further blocks are required.
    *
    */ /**
    * Property to indicate if the response should always include the Block2 option when client request early blockwise negociation but the response can be sent on one packet.
    * <p>
    * The default value of this property is
    * {@link NetworkConfigDefaults#DEFAULT_BLOCKWISE_STRICT_BLOCK2_OPTION}.
    * <p>
    * A value of {@code false} indicate that the server will respond without block2 option if no further blocks are required.<br/>
    * A value of {@code true} indicate that the server will response with block2 option event if no further blocks are required.
    *
    */
  val BLOCKWISE_STRICT_BLOCK2_OPTION: String = "BLOCKWISE_STRICT_BLOCK2_OPTION"

  val NOTIFICATION_CHECK_INTERVAL_TIME: String = "NOTIFICATION_CHECK_INTERVAL"

  val NOTIFICATION_CHECK_INTERVAL_COUNT: String =
    "NOTIFICATION_CHECK_INTERVAL_COUNT"

  val NOTIFICATION_REREGISTRATION_BACKOFF: String =
    "NOTIFICATION_REREGISTRATION_BACKOFF"

  val USE_CONGESTION_CONTROL: String = "USE_CONGESTION_CONTROL"

  val CONGESTION_CONTROL_ALGORITHM: String = "CONGESTION_CONTROL_ALGORITHM"

  val PROTOCOL_STAGE_THREAD_COUNT: String = "PROTOCOL_STAGE_THREAD_COUNT"

  val NETWORK_STAGE_RECEIVER_THREAD_COUNT: String =
    "NETWORK_STAGE_RECEIVER_THREAD_COUNT"

  val NETWORK_STAGE_SENDER_THREAD_COUNT: String =
    "NETWORK_STAGE_SENDER_THREAD_COUNT"

  val UDP_CONNECTOR_DATAGRAM_SIZE: String = "UDP_CONNECTOR_DATAGRAM_SIZE"

  val UDP_CONNECTOR_RECEIVE_BUFFER: String = "UDP_CONNECTOR_RECEIVE_BUFFER"

  val UDP_CONNECTOR_SEND_BUFFER: String = "UDP_CONNECTOR_SEND_BUFFER"

  val UDP_CONNECTOR_OUT_CAPACITY: String = "UDP_CONNECTOR_OUT_CAPACITY"

  val DEDUPLICATOR: String = "DEDUPLICATOR"

  val DEDUPLICATOR_MARK_AND_SWEEP: String = "DEDUPLICATOR_MARK_AND_SWEEP"

  /**
    * The interval after which the next sweep run should occur (in
    * MILLISECONDS).
    */ /**
    * The interval after which the next sweep run should occur (in
    * MILLISECONDS).
    */
  val MARK_AND_SWEEP_INTERVAL: String = "MARK_AND_SWEEP_INTERVAL"

  val DEDUPLICATOR_CROP_ROTATION: String = "DEDUPLICATOR_CROP_ROTATION"

  /**
    * The interval after which the next crop run should occur (in
    * MILLISECONDS).
    */ /**
    * The interval after which the next crop run should occur (in
    * MILLISECONDS).
    */
  val CROP_ROTATION_PERIOD: String = "CROP_ROTATION_PERIOD"

  val NO_DEDUPLICATOR: String = "NO_DEDUPLICATOR"

  val DEDUPLICATOR_AUTO_REPLACE: String = "DEDUPLICATOR_AUTO_REPLACE"

  val RESPONSE_MATCHING: String = "RESPONSE_MATCHING"

  val HTTP_PORT: String = "HTTP_PORT"

  val HTTP_SERVER_SOCKET_TIMEOUT: String = "HTTP_SERVER_SOCKET_TIMEOUT"

  val HTTP_SERVER_SOCKET_BUFFER_SIZE: String = "HTTP_SERVER_SOCKET_BUFFER_SIZE"

  val HTTP_CACHE_RESPONSE_MAX_AGE: String = "HTTP_CACHE_RESPONSE_MAX_AGE"

  val HTTP_CACHE_SIZE: String = "HTTP_CACHE_SIZE"

  val HEALTH_STATUS_INTERVAL: String = "HEALTH_STATUS_INTERVAL"

  /**
 Properties for TCP connector.
    */ /**
 Properties for TCP connector.
    */
  val TCP_CONNECTION_IDLE_TIMEOUT: String = "TCP_CONNECTION_IDLE_TIMEOUT"

  val TCP_CONNECT_TIMEOUT: String = "TCP_CONNECT_TIMEOUT"

  val TCP_WORKER_THREADS: String = "TCP_WORKER_THREADS"

  val TLS_HANDSHAKE_TIMEOUT: String = "TLS_HANDSHAKE_TIMEOUT"

  /**
    * (D)TLS session timeout in seconds.
    */ /**
    * (D)TLS session timeout in seconds.
    */
  val SECURE_SESSION_TIMEOUT: String = "SECURE_SESSION_TIMEOUT"

  /**
    * DTLS auto resumption timeout in milliseconds. After that period
    * without exchanged messages, the session is forced to resume.
    */ /**
    * DTLS auto resumption timeout in milliseconds. After that period
    * without exchanged messages, the session is forced to resume.
    */
  val DTLS_AUTO_RESUME_TIMEOUT: String = "DTLS_AUTO_RESUME_TIMEOUT"

  /**
    * DTLS connection id length.
    *
    * <a https://tools.ietf.org/html/draft-ietf-tls-dtls-connection-id-02>
    * draft-ietf-tls-dtls-connection-id-02</a>
    *
    * <ul>
    * <li>{@code ""} disabled support for connection id.</li>
    * <li>{@code 0} enable support for connection id, but don't use it for
    * incoming traffic to this peer.</li>
    * <li>{@code n} use connection id of n bytes. Note: chose n large
    * enough for the number of considered peers. Recommended to have 100
    * time more values than peers. E.g. 65000 peers, chose not 2 bytes,
    * chose at lease 3 bytes!</li>
    * </ul>
    */ /**
    * DTLS connection id length.
    *
    * <a https://tools.ietf.org/html/draft-ietf-tls-dtls-connection-id-02>
    * draft-ietf-tls-dtls-connection-id-02</a>
    *
    * <ul>
    * <li>{@code ""} disabled support for connection id.</li>
    * <li>{@code 0} enable support for connection id, but don't use it for
    * incoming traffic to this peer.</li>
    * <li>{@code n} use connection id of n bytes. Note: chose n large
    * enough for the number of considered peers. Recommended to have 100
    * time more values than peers. E.g. 65000 peers, chose not 2 bytes,
    * chose at lease 3 bytes!</li>
    * </ul>
    */
  val DTLS_CONNECTION_ID_LENGTH: String = "DTLS_CONNECTION_ID_LENGTH"

  /**
    * If {@link #DTLS_CONNECTION_ID_LENGTH} enables the use of a connection
    * id, this node id could be used to configure the generation of
    * connection ids specific for node in a multi-node deployment
    * (cluster). The value is used as first byte in generated connection
    * ids.
    */ /**
    * If {@link #DTLS_CONNECTION_ID_LENGTH} enables the use of a connection
    * id, this node id could be used to configure the generation of
    * connection ids specific for node in a multi-node deployment
    * (cluster). The value is used as first byte in generated connection
    * ids.
    */
  val DTLS_CONNECTION_ID_NODE_ID: String = "DTLS_CONNECTION_ID_NODE_ID"

}
