(ns s-exp.hirundo.options
  (:import (io.helidon.common.concurrency.limits Limit)
           (io.helidon.common.socket SocketOptions$Builder)
           (io.helidon.common.tls Tls)
           (io.helidon.webserver WebServerConfig$Builder)
           (java.time Duration)))

(set! *warn-on-reflection* true)

(defmulti set-server-option! (fn [_builder k _v _options] k))

(defmethod set-server-option! :default [builder _ _ _]
  builder)

(defmethod set-server-option! :host
  [^WebServerConfig$Builder builder _ host _]
  (.host builder host))

(defmethod set-server-option! :port
  [^WebServerConfig$Builder builder _ port _]
  (.port builder (int port)))

(defmethod set-server-option! :backlog
  [^WebServerConfig$Builder builder _ backlog _]
  (.backlog builder (int backlog)))

(defmethod set-server-option! :idle-connection-timeout
  [^WebServerConfig$Builder builder _ idle-connection-timeout-ms _]
  (.idleConnectionTimeout builder (Duration/ofMillis idle-connection-timeout-ms)))

(defmethod set-server-option! :idle-connection-period
  [^WebServerConfig$Builder builder _ idle-connection-period-ms _]
  (.idleConnectionPeriod builder (Duration/ofMillis idle-connection-period-ms)))

(defmethod set-server-option! :max-payload-size
  [^WebServerConfig$Builder builder _ max-payload-size _]
  (.maxPayloadSize builder (long max-payload-size)))

(defmethod set-server-option! :max-tcp-connections
  [^WebServerConfig$Builder builder _ max-tcp-connections _]
  (.maxTcpConnections builder (int max-tcp-connections)))

(defmethod set-server-option! :concurrency-limit
  [^WebServerConfig$Builder builder _ ^Limit concurrency-limit _]
  (.concurrencyLimit builder concurrency-limit))

(defmethod set-server-option! :max-concurrent-requests
  [^WebServerConfig$Builder builder _ max-concurrent-requests _]
  (.maxConcurrentRequests builder (long max-concurrent-requests)))

(defmethod set-server-option! :max-in-memory-entity
  [^WebServerConfig$Builder builder _ max-in-memory-entity _]
  (.maxInMemoryEntity builder (int max-in-memory-entity)))

(defmethod set-server-option! :smart-async-writes
  [^WebServerConfig$Builder builder _ smart-async-writes _]
  (.smartAsyncWrites builder (boolean smart-async-writes)))

(defmethod set-server-option! :write-queue-length
  [^WebServerConfig$Builder builder _ write-queue-length _]
  (.writeQueueLength builder (long write-queue-length)))

(defmethod set-server-option! :write-buffer-size
  [^WebServerConfig$Builder builder _ write-buffer-size _]
  (.writeBufferSize builder (int write-buffer-size)))

(defn- set-connection-options!
  [^SocketOptions$Builder socket-options-builder
   {:keys [socket-receive-buffer-size socket-send-buffer-size
           socket-reuse-address socket-keep-alive tcp-no-delay
           read-timeout connect-timeout]}]
  (when socket-receive-buffer-size
    (.socketReceiveBufferSize socket-options-builder
                              (int socket-receive-buffer-size)))

  (when socket-send-buffer-size
    (.socketSendBufferSize socket-options-builder
                           (int socket-send-buffer-size)))

  (when (some? socket-reuse-address)
    (.socketReuseAddress socket-options-builder
                         (boolean socket-reuse-address)))

  (when (some? socket-keep-alive)
    (.socketKeepAlive socket-options-builder
                      (boolean socket-keep-alive)))
  (when (some? tcp-no-delay)
    (.tcpNoDelay socket-options-builder
                 (boolean tcp-no-delay)))

  (when read-timeout
    (.readTimeout socket-options-builder
                  (Duration/ofMillis read-timeout)))
  (when connect-timeout
    (.connectTimeout socket-options-builder
                     (Duration/ofMillis connect-timeout))))

(defmethod set-server-option! :connection-options
  [^WebServerConfig$Builder builder _ connection-options _]
  (.connectionOptions builder
                      (reify java.util.function.Consumer
                        (accept [_ socket-options-builder]
                          (set-connection-options! socket-options-builder
                                                   connection-options)))))

(defmethod set-server-option! :tls
  [^WebServerConfig$Builder builder _ tls-config _]
  (doto builder (.tls ^Tls tls-config)))
