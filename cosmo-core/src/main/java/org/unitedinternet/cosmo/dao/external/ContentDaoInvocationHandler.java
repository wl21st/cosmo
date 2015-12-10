package org.unitedinternet.cosmo.dao.external;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.util.Arrays;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.unitedinternet.cosmo.dao.ContentDao;
import org.unitedinternet.cosmo.model.CollectionItem;
import org.unitedinternet.cosmo.model.filter.NoteItemFilter;

/**
 * <code>InvocationHandler</code> that delegates the method calls to the internal DAO object or to an external DAO
 * provider.
 * 
 * @author daniel grigore
 *
 */
public class ContentDaoInvocationHandler implements InvocationHandler {

    private static final Log LOG = LogFactory.getLog(ContentDaoInvocationHandler.class);

    /**
     * Database DAO.
     */
    private final ContentDao contentDaoInternal;

    private final ContentDao contentDaoExternal;

    public ContentDaoInvocationHandler(ContentDao contentDaoInternal, ContentDao contentDaoExternal) {
        super();
        this.contentDaoInternal = contentDaoInternal;
        this.contentDaoExternal = contentDaoExternal;
    }

    @Override
    public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
        if (method.isAnnotationPresent(ExternalizableContent.class)) {
            String uuid = getExternalUuid(args);
            if (uuid != null) {
                LOG.info("EXTERNAL calling method " + method.getName() + " with args: " + Arrays.toString(args)
                        + " for external uuid: " + uuid);
                return method.invoke(this.contentDaoExternal, args);
            }
        }
        return method.invoke(this.contentDaoInternal, args);
    }

    private static String getExternalUuid(Object[] args) {
        if (args != null) {
            for (Object arg : args) {
                String path = null;
                if (arg instanceof String) {
                    // Method findItemByPath(String) or findItemByPath(String)
                    path = (String) arg;
                } else if (arg instanceof NoteItemFilter) {
                    // Method findItems(NoteItemFilter)
                    NoteItemFilter filter = (NoteItemFilter) arg;
                    CollectionItem parent = filter.getParent();
                    if (parent != null) {
                        path = parent.getUid();
                    }
                }
                if (path != null && CalendarUuidGenerator.containsExternalUid(path)) {
                    return CalendarUuidGenerator.extractUuid(path);
                }
            }
        }
        return null;
    }
}