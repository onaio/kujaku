package io.ona.kujaku.utils.helpers;

import org.simpleframework.xml.Serializer;
import org.simpleframework.xml.core.Persister;

import java.io.Reader;

/**
 * Class used to deserialize an Xml document into an object.
 *
 * Created by Emmanuel Otin - eo@novel-t.ch 11/29/18.
 */
public class WmtsCapabilitiesSerializer {

    public WmtsCapabilitiesSerializer(){
    }

    /**
     * This <code>read</code> method will read the contents of the XML
     * document from the provided source and convert it into an object
     * of the specified type. If the XML source cannot be deserialized
     * or there is a problem building the object graph an exception
     * is thrown. The instance deserialized is returned.
     *
     * @param type this is the class type to be deserialized from XML
     * @param source this provides the source of the XML document
     * @param strict this determines whether to read in strict mode
     *
     * @return the object deserialized from the XML document
     *
     * @throws Exception if the object cannot be fully deserialized
     */
    public <T> T read(Class<? extends T> type, Reader source, boolean strict) throws Exception {
        Serializer serializer = new Persister();
        return serializer.read(type, source, strict);
    }
}
