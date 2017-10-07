/**
 * Code courtesy of W. Biller
 * See http://wbiller.bitbucket.org/blog/mongofactory/index.html
 */

package com.hilats.server.spring;

import com.mongodb.MongoClient;
import de.flapdoodle.embed.mongo.Command;
import de.flapdoodle.embed.mongo.MongodExecutable;
import de.flapdoodle.embed.mongo.MongodProcess;
import de.flapdoodle.embed.mongo.MongodStarter;
import de.flapdoodle.embed.mongo.config.*;
import de.flapdoodle.embed.mongo.distribution.IFeatureAwareVersion;
import de.flapdoodle.embed.mongo.distribution.Version;
import de.flapdoodle.embed.process.config.IRuntimeConfig;
import de.flapdoodle.embed.process.config.io.ProcessOutput;
import de.flapdoodle.embed.process.extract.UUIDTempNaming;
import de.flapdoodle.embed.process.io.IStreamProcessor;
import de.flapdoodle.embed.process.runtime.Network;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.config.AbstractFactoryBean;

import java.io.File;

/**
 * EmbeddedMongoFactoryBean will start an embedded MongoDB instance and returns 
 * a singleton instance of {@link com.mongodb.Mongo}.
 *
 * @author wbiller
 */
public class EmbeddedMongoFactoryBean extends AbstractFactoryBean<MongoClient> {

    private static abstract class AbstractSLF4JStreamProcessor implements IStreamProcessor {

        protected Logger logger;

        protected AbstractSLF4JStreamProcessor(Logger logger) {
            this.logger = logger;
        }

        @Override
        public void onProcessed() { }

        public String clean(String l) {
            return l.replaceAll("[\n\r]+", "");
        }
    }

    private static final class TraceSLF4JStreamProcessor extends AbstractSLF4JStreamProcessor {

        public TraceSLF4JStreamProcessor(Logger logger) {
            super(logger);
        }

        @Override
        public void process(String arg0) {
            logger.trace(clean(arg0));
        }
    }

    private static final class ErrorSLF4JStreamProcessor extends AbstractSLF4JStreamProcessor {

        protected ErrorSLF4JStreamProcessor(Logger logger) {
            super(logger);
        }

        @Override
        public void process(String arg0) {
            logger.error(clean(arg0));
        }
    }

    private static final class InfoSLF4JStreamProcessor extends AbstractSLF4JStreamProcessor {

        protected InfoSLF4JStreamProcessor(Logger logger) {
            super(logger);
        }

        @Override
        public void process(String arg0) {
            logger.info(clean(arg0));
        }
    }

    private MongodExecutable exe = null;

    private String host = "localhost";
    private int port = 27017;
    private String dbPath = null;

    private IFeatureAwareVersion version = Version.Main.PRODUCTION;

    /* (non-Javadoc)
     * @see org.springframework.beans.factory.config.AbstractFactoryBean#afterPropertiesSet()
     */
    @Override
    public void afterPropertiesSet() throws Exception {

        Logger mdbLogger = LoggerFactory.getLogger(MongodProcess.class);
        IRuntimeConfig runtimeConfig =
                new RuntimeConfigBuilder()
                        .defaults(Command.MongoD)
                        .processOutput(
                                new ProcessOutput(new TraceSLF4JStreamProcessor(mdbLogger),
                                        new ErrorSLF4JStreamProcessor(mdbLogger),
                                        new InfoSLF4JStreamProcessor(mdbLogger)))
                        .artifactStore(
                                new ArtifactStoreBuilder().defaults(Command.MongoD)
                                        .download(new DownloadConfigBuilder()
                                                .defaultsForCommand(Command.MongoD))
                                        .executableNaming(new UUIDTempNaming()))
                        .build();

        MongodStarter starter = MongodStarter.getInstance(runtimeConfig);


        MongodConfigBuilder builder = new MongodConfigBuilder()
                .version(version)
                .net(new Net(host, port, Network.localhostIsIPv6()));

        if (dbPath != null) {
            builder.replication(new Storage(dbPath, null, 0));

            // delete lock file id remaining from previous run
            File lockFile = new File(dbPath, "mongod.lock");
            if (lockFile.exists())
                try {
                    lockFile.delete();
                } catch (Exception e) {
                    logger.warn("Failed to remove mongod lock file", e);
                }
        }

        IMongodConfig mongodConfig = builder.build();

        exe = starter.prepare(mongodConfig);

        exe.start();

        super.afterPropertiesSet();
    }

    /* (non-Javadoc)
     * @see org.springframework.beans.factory.config.AbstractFactoryBean#createInstance()
     */
    @Override
    protected MongoClient createInstance() throws Exception {
        return new MongoClient(host, port);
    }

    /* (non-Javadoc)
     * @see org.springframework.beans.factory.config.AbstractFactoryBean#destroy()
     */
    @Override
    public void destroy() throws Exception {
        super.destroy();
        if(exe != null)
            exe.stop();
    }

    /* (non-Javadoc)
     * @see org.springframework.beans.factory.config.AbstractFactoryBean#destroyInstance(java.lang.Object)
     */
    @Override
    protected void destroyInstance(MongoClient instance) throws Exception {
        if(instance != null)
            instance.close();
    }

    /* (non-Javadoc)
     * @see org.springframework.beans.factory.config.AbstractFactoryBean#getObjectType()
     */
    @Override
    public Class<?> getObjectType() {
        return MongoClient.class;
    }

    public void setHost(String host) {
        this.host = host;
    }

    public void setPort(int port) {
        this.port = port;
    }

    public void setVersion(IFeatureAwareVersion version) {
        this.version = version;
    }

    public void setDbPath(String dbPath) {
        this.dbPath = dbPath;
    }
}