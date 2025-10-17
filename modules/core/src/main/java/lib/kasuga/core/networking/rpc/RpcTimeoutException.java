package lib.kasuga.core.networking.rpc;

public class RpcTimeoutException extends Exception {
    public RpcTimeoutException(String s) {
        super(s);
    }
}
