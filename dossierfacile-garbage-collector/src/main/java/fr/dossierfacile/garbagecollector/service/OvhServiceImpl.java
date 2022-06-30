package fr.dossierfacile.garbagecollector.service;

import fr.dossierfacile.garbagecollector.exceptions.OvhConnectionFailedException;
import fr.dossierfacile.garbagecollector.service.interfaces.OvhService;
import io.sentry.Sentry;
import lombok.extern.slf4j.Slf4j;
import org.openstack4j.api.OSClient;
import org.openstack4j.api.exceptions.AuthenticationException;
import org.openstack4j.api.exceptions.ClientResponseException;
import org.openstack4j.api.storage.ObjectStorageObjectService;
import org.openstack4j.model.common.Identifier;
import org.openstack4j.model.storage.object.SwiftObject;

import org.openstack4j.model.storage.object.options.ObjectLocation;
import org.openstack4j.openstack.OSFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@Slf4j
public class OvhServiceImpl implements OvhService {
    private static final String OVH_CONNECT = "OVH connect. ";
    private static final String EXCEPTION = "Sentry ID Exception: ";

    @Value("${ovh.project.domain:default}")
    private String ovhProjectDomain;
    @Value("${ovh.auth.url:default}")
    private String ovhAuthUrl;
    @Value("${ovh.username:default}")
    private String ovhUsername;
    @Value("${ovh.password:default}")
    private String ovhPassword;
    @Value("${ovh.project.name:default}")
    private String ovhProjectName;
    @Value("${ovh.region:default}")
    private String ovhRegion;
    @Value("${ovh.container:default}")
    private String ovhContainerName;
    @Value("${ovh.connection.reattempts:3}")
    private Integer ovhConnectionReattempts;
    private String tokenId;

    private OSClient.OSClientV3 connect() {
        OSClient.OSClientV3 os = null;
        Identifier domainIdentifier = Identifier.byId(ovhProjectDomain);
        int attempts = 0;
        while (attempts++ < ovhConnectionReattempts && os == null) {
            try {
                if (tokenId == null) {
                    os = OSFactory.builderV3()
                            .endpoint(ovhAuthUrl)
                            .credentials(ovhUsername, ovhPassword, domainIdentifier)
                            .scopeToProject(Identifier.byName(ovhProjectName), Identifier.byName(ovhProjectDomain))
                            .authenticate();
                    os.useRegion(ovhRegion);
                    tokenId = os.getToken().getId();
                } else {
                    os = OSFactory.builderV3()
                            .endpoint(ovhAuthUrl)
                            .token(tokenId)
                            .scopeToProject(Identifier.byName(ovhProjectName), Identifier.byName(ovhProjectDomain))
                            .authenticate();
                    os.useRegion(ovhRegion);
                }
            } catch (AuthenticationException | ClientResponseException e) {
                if (e instanceof ClientResponseException) {
                    log.error(e.toString());
                }
                os = OSFactory.builderV3()
                        .endpoint(ovhAuthUrl)
                        .credentials(ovhUsername, ovhPassword, domainIdentifier)
                        .scopeToProject(Identifier.byName(ovhProjectName), Identifier.byName(ovhProjectDomain))
                        .authenticate();
                os.useRegion(ovhRegion);
                tokenId = os.getToken().getId();
            } catch (Exception e) {
                log.error(e.getMessage());
                if (attempts == ovhConnectionReattempts) {
                    log.error(OVH_CONNECT + EXCEPTION + Sentry.captureException(e));
                    log.error(e.getClass().getName());
                    String customExceptionMessage = OVH_CONNECT + "Could not connect to the storage provider after " + attempts + " attempts with given credentials";
                    throw new OvhConnectionFailedException(customExceptionMessage, e.getCause());
                }
            }
        }
        return os;
    }

    @Override
    @Async
    public void delete(String name) {
        connect().objectStorage().objects().delete(ovhContainerName, name);
    }

    @Override
    @Async
    public void delete(List<String> name) {
        name.forEach(this::delete);
    }

    @Override
    public SwiftObject get(String name) {
        return connect().objectStorage().objects().get(ovhContainerName, name);
    }

    @Override
    public ObjectStorageObjectService getObjectStorage() {
        OSClient.OSClientV3 os = connect();
        return os.objectStorage().objects();
    }

    @Override
    public void renameFile(String oldName, String newName) {
        int attempts = 0;
        final String REMOVE_LATER = "REMOVE_LATER. ";
        log.info(REMOVE_LATER + "INITIALIZING [attempts] to zero");
        while (attempts++ < ovhConnectionReattempts) {
            log.info(REMOVE_LATER + "attempt number [" + attempts + "]");
            try {
                final ObjectStorageObjectService objService = getObjectStorage();
                log.info(REMOVE_LATER + "Before calling the copy method to rename [" + oldName + "] to [" + newName + "]");
                String eTag = objService.copy(ObjectLocation.create(ovhContainerName, oldName), ObjectLocation.create(ovhContainerName, newName));
                log.info(REMOVE_LATER + "eTAG [" + eTag + "]");
                if (eTag != null && !eTag.isEmpty()) {
                    log.info(REMOVE_LATER + "Before deleting the old file [" + oldName + "]");
                    objService.delete(ovhContainerName, oldName);
                    log.info("[" + oldName + "] renamed to [" + newName + "]");
                    return;
                }
                log.error("[" + oldName + "] was not renamed successfully");
                String customExceptionMessage = OVH_CONNECT + "Could not rename the file [" + oldName + "]";
                throw new OvhConnectionFailedException(customExceptionMessage);
            } catch (Exception e) {
                log.error(e.getMessage());
                if (attempts == ovhConnectionReattempts) {
                    log.error(OVH_CONNECT + EXCEPTION + Sentry.captureException(e));
                    log.error(e.getClass().getName());
                    String customExceptionMessage = OVH_CONNECT + "Could not connect to the storage provider after " + attempts + " attempts with given credentials";
                    throw new OvhConnectionFailedException(customExceptionMessage, e.getCause());
                }
                try {
                    log.info("Waiting 60 seconds for the next retry...");
                    Thread.sleep(60000);
                } catch (InterruptedException b) {
                    log.error(b.getMessage());
                    log.error("Unable to sleep the process");
                }
            }
        }
    }

}
