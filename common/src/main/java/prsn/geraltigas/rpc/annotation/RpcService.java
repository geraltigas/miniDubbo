package prsn.geraltigas.rpc.annotation;

import java.lang.annotation.Retention;
import java.lang.annotation.Target;

/**
 * used for expose the service interface
 */
@Target({java.lang.annotation.ElementType.TYPE})
@Retention(java.lang.annotation.RetentionPolicy.RUNTIME)
public @interface RpcService {

}
