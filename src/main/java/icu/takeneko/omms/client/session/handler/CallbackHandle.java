package icu.takeneko.omms.client.session.handler;

/**
 * Parse messages provided by context and invoke the callback
 * @param <C> context class
 */
public abstract class CallbackHandle<C> {

    abstract public void invoke(C context);
}
