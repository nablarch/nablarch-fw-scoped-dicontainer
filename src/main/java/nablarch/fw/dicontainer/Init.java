package nablarch.fw.dicontainer;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * コンポーネントの初期化を行うライフサイクルメソッドであることを表すアノテーション。
 * 
 * <p>{@code @Init}を付与するメソッドは次の制約を守らなければならない。</p>
 * <ul>
 * <li>staticメソッドではないこと</li>
 * <li>引数がないこと</li>
 * </ul>
 * 
 * <p>また、1つのコンポーネント内で{@code @Init}を付与できるメソッドは1つだけに限られる。</p>
 *
 */
@Documented
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.METHOD)
public @interface Init {
}
