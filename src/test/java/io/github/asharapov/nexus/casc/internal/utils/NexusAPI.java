package io.github.asharapov.nexus.casc.internal.utils;

import io.github.asharapov.nexus.casc.internal.model.AnonymousAccessVO;
import io.github.asharapov.nexus.casc.internal.model.BlobStoreVO;
import io.github.asharapov.nexus.casc.internal.model.CertificateVO;
import io.github.asharapov.nexus.casc.internal.model.ContentSelectorVO;
import io.github.asharapov.nexus.casc.internal.model.EmailVO;
import io.github.asharapov.nexus.casc.internal.model.IqConnectionVO;
import io.github.asharapov.nexus.casc.internal.model.LdapServerVO;
import io.github.asharapov.nexus.casc.internal.model.LicenseVO;
import io.github.asharapov.nexus.casc.internal.model.PrivilegeVO;
import io.github.asharapov.nexus.casc.internal.model.RealmVO;
import io.github.asharapov.nexus.casc.internal.model.RepositoryVO;
import io.github.asharapov.nexus.casc.internal.model.ResultVO;
import io.github.asharapov.nexus.casc.internal.model.RoleVO;
import io.github.asharapov.nexus.casc.internal.model.RoutingRuleVO;
import io.github.asharapov.nexus.casc.internal.model.S3BlobStoreVO;
import io.github.asharapov.nexus.casc.internal.model.TaskListVO;
import io.github.asharapov.nexus.casc.internal.model.UserVO;
import okhttp3.RequestBody;
import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.GET;
import retrofit2.http.Headers;
import retrofit2.http.POST;
import retrofit2.http.Path;
import retrofit2.http.Query;

import java.util.List;

/**
 * Simple client for Nexus REST API to perform tests.
 *
 * @author Anton Sharapov
 */
public interface NexusAPI {

    @GET("casc/config")
    Call<ResponseBody> getConfiguration();

    @GET("casc/config")
    Call<ResponseBody> getConfiguration(@Query("showReadOnlyObjects") boolean showReadOnlyObjects);

    @Headers("Content-Type: text/vnd.yaml")
    @POST("casc/config")
    Call<ResponseBody> applyConfiguration(@Body RequestBody cfg);

    @GET("v1/status")
    Call<Void> checkStatus();

    @GET("beta/email")
    Call<EmailVO> getEmail();

    @POST("beta/email/verify")
    Call<ResultVO> verifyEmail(@Body RequestBody email);

    @GET("v1/iq")
    Call<IqConnectionVO> getIqConnection();

    @GET("beta/security/anonymous")
    Call<AnonymousAccessVO> getAnonymousStatus();

    @GET("v1/security/ldap")
    Call<List<LdapServerVO>> getLdapServers();

    @GET("beta/security/realms/available")
    Call<List<RealmVO>> getAvailableRealms();

    @GET("beta/security/realms/active")
    Call<List<String>> getActiveRealmIds();

    @GET("beta/security/privileges")
    Call<List<PrivilegeVO>> getPrivileges();

    @GET("beta/security/roles")
    Call<List<RoleVO>> getRoles(@Query("source") String source);

    @GET("beta/security/users")
    Call<List<UserVO>> getUsers(@Query("source") String source, @Query("userId") String userId);

    @GET("beta/security/ssl/truststore")
    Call<List<CertificateVO>> getTrustedCertificates();

    @GET("v1/blobstores")
    Call<List<BlobStoreVO>> getBlobStores();

    @GET("v1/blobstores/s3/{name}")
    Call<S3BlobStoreVO> getS3BlobStoreInfo(@Path("name") String blobStoreName);

    @GET("beta/security/content-selectors")
    Call<List<ContentSelectorVO>> getContentSelectors();

    @GET("v1/routing-rules")
    Call<List<RoutingRuleVO>> getRoutingRules();

    /**
     * Shows all scheduled tasks that are visible (including not exposed to users)
     */
    @GET("v1/tasks")
    Call<TaskListVO> getTasks();

    /**
     * Start specified task
     */
    @POST("v1/tasks/{id}/run")
    Call<ResponseBody> fireTask(@Path("id") String taskId);


    @GET("beta/repositories")
    Call<List<RepositoryVO>> getRepositories();


    @GET("v1/system/license")
    Call<LicenseVO> getLicense();
}
