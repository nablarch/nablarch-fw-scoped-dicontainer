package nablarch.fw.dicontainer.web;

import java.lang.annotation.Documented;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

import javax.inject.Scope;


/**
 * コンポーネントがセッションスコープであることを表すアノテーション。
 * 
 */
@Scope
@Documented
@Retention(RetentionPolicy.RUNTIME)
public @interface SessionScoped {
}
