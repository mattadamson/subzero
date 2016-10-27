package info.jerrinot.subzero;

import com.hazelcast.core.HazelcastInstance;
import com.hazelcast.core.HazelcastInstanceAware;
import com.hazelcast.nio.ObjectDataInput;
import com.hazelcast.nio.ObjectDataOutput;
import com.hazelcast.nio.serialization.StreamSerializer;
import info.jerrinot.subzero.internal.strategy.KryoStrategy;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

abstract class AbstractSerializer<T> implements StreamSerializer<T>, HazelcastInstanceAware {
    private int autoGeneratedTypeId;
    private HazelcastInstance hazelcastInstance;
    private KryoStrategy<T> strategy;

    AbstractSerializer(KryoStrategy<T> strategy) {
        this.strategy = strategy;
    }

    @Override
    public final void write(ObjectDataOutput out, T object) throws IOException {
        strategy.write((OutputStream) out, object);
    }

    @Override
    public final T read(ObjectDataInput in) throws IOException {
        return strategy.read((InputStream) in);
    }

    /**
     * Override this method to returns your own type ID.
     * <p>
     * Default implementation relies on serializers registration order - all
     * your cluster members have to register SubZero in the same order otherwise
     * things will get out-of-sync you you will get tons of serializations errors.
     * <p>
     * All serializers registered in Hazelcast have to return a unique type ID.
     *
     * @return serializer type ID.
     */
    @Override
    public int getTypeId() {
        return autoGeneratedTypeId;
    }

    @Override
    public final void destroy() {
        strategy.destroy(hazelcastInstance);
    }

    @Override
    public final void setHazelcastInstance(HazelcastInstance hazelcastInstance) {
        strategy.setHazelcastInstance(hazelcastInstance);
        this.hazelcastInstance = hazelcastInstance;
        this.autoGeneratedTypeId = strategy.newId();
    }
}