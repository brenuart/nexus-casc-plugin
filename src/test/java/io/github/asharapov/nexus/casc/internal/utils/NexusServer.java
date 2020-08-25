package io.github.asharapov.nexus.casc.internal.utils;

import com.github.dockerjava.api.command.InspectContainerResponse;
import com.github.dockerjava.api.exception.NotFoundException;
import io.github.asharapov.nexus.casc.internal.Utils;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.containers.wait.strategy.Wait;
import org.testcontainers.utility.MountableFile;

import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

/**
 * Represents a Sonatype Nexus docker instance with installed CASC plugin.
 *
 * @author Anton Sharapov
 */
public class NexusServer extends GenericContainer<NexusServer> {

    private static final String IMAGE_NAME = "sonatype/nexus3:" + System.getProperty("nexus.docker.version", "latest");

    private final ConcurrentMap<String, NexusAPI> clientAPIs;
    private volatile String adminPassword;

    public NexusServer() {
        super(IMAGE_NAME);
        this.clientAPIs = new ConcurrentHashMap<>();
        withExposedPorts(8081);
        waitingFor(Wait.forListeningPort());

        mountFile(TestUtils.getCascPluginFile(), "/opt/sonatype/nexus/deploy/");
        for (MountableFile pem : TestUtils.getPemFiles()) {
            mountFile(pem, "/opt/certs/");
        }
//        withEnv("NEXUS_SECURITY_RANDOMPASSWORD", "false");
        withEnv("BASE_URL", "http://localhost:8081");
    }

    public void mountFile(final MountableFile mf, String targetDir) {
        if (!targetDir.endsWith("/")) {
            targetDir += "/";
        }
        String path = mf.getFilesystemPath();
        int s = path.lastIndexOf('/');
        String fileName = s >= 0 ? path.substring(s + 1) : path;
        withCopyFileToContainer(mf, targetDir + fileName);
    }

    public String getDefaultAdminPassword() {
        String result = adminPassword;
        if (result == null) {
            final boolean useRandomPassword = Boolean.parseBoolean(getEnvMap().getOrDefault("NEXUS_SECURITY_RANDOMPASSWORD", "true"));
            if (useRandomPassword) {
                result = this.copyFileFromContainer("/nexus-data/admin.password",
                        in -> new String(Utils.load(in), StandardCharsets.UTF_8));
            } else {
                result = "admin123";
            }
            adminPassword = result;
        }
        return result;
    }
    public void setDefaultAdminPassword(final String adminPassword) {
        this.adminPassword = adminPassword;
    }

    public String getInternalHostName() {
        final InspectContainerResponse icr = this.getContainerInfo();
        if (icr == null)
            throw new IllegalStateException("Nexus container not started yet");
        String host = icr.getName();
        if (host.startsWith("/")) {
            host = host.substring(1);
        }
        return host;
    }

    public NexusAPI getAPI(final String user, final String password) {
        final String effectiveUser = user != null ? user : "";
        return clientAPIs.computeIfAbsent(effectiveUser, key ->
                TestUtils.makeApi("localhost", getMappedPort(8081), user, password)
        );
    }

    public NexusAPI getAdminAPI() {
        return getAPI("admin", getDefaultAdminPassword());
    }

    public String getPluginTaskResult() throws InterruptedException {
        final String path = "/nexus-data/casc/export/nexus.yml";
        byte[] data1, data2;
        do {
            try {
                data1 = this.copyFileFromContainer(path, Utils::load);
            } catch (NotFoundException e) {
                data1 = null;
            }
            //noinspection BusyWait
            Thread.sleep(100);
            try {
                data2 = this.copyFileFromContainer(path, Utils::load);
            } catch (NotFoundException e) {
                data2 = null;
            }
        } while (!Arrays.equals(data1, data2));
        return data2 != null ? new String(data2, StandardCharsets.UTF_8) : null;
    }

    @Override
    protected void containerIsStopped(final InspectContainerResponse containerInfo) {
        super.containerIsStopped(containerInfo);
        this.clientAPIs.clear();
    }

}
