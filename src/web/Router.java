package web;

import stats.Stats;

import java.util.ArrayList;
import java.util.List;

/**
 * The Router maps incoming requests to registered ServiceHandlers.
 */
class Router {
    private List<Class<? extends ServiceHandler>> handlers;

    /** Construct a new Router. */
    Router() {
        handlers = new ArrayList<Class<? extends ServiceHandler>>();
    }

    /** Return the ServiceHandler that matches the incoming Request's path. */
    public ServiceHandler route(Request r) {
        for (Class<? extends ServiceHandler> klass : handlers) {
            try {
                ServiceHandler sh = klass.newInstance();

                if (r.getPath().equals(sh.getPath())) {
                    return sh;
                }
            } catch (InstantiationException e) {
                e.printStackTrace();
            } catch (IllegalAccessException e) {
                e.printStackTrace();
            }
        }

        return new ErrorServiceHandler();
    }

    /** Register a new ServiceHandler to be returned by route(). */
    public void register(Class<? extends ServiceHandler> klass) {
        Stats.hit("web.router.handlers", 1);
        handlers.add(klass);
    }
}
