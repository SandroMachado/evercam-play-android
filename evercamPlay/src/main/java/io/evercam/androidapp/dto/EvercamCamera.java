package io.evercam.androidapp.dto;

import android.util.Log;

import org.apache.http.cookie.Cookie;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Locale;

import io.evercam.Camera;
import io.evercam.EvercamException;

public class EvercamCamera
{
    public ImageLoadingStatus loadingStatus = ImageLoadingStatus.not_started;
    public ArrayList<Cookie> cookies = null;

    private final String TAG = "EvercamCamera";
    private boolean isLocal = false;
    public Camera camera = null;
    private int id = -1;

    private String cameraId = "";
    private String name = "";
    private String owner = ""; // The user's user name
    private String realOwner = "";// The owner of camera
    private boolean canEdit = false;
    private boolean canDelete = false;
    private String username = "";
    private String password = "";
    private String timezone = "";
    private String vendor = "";
    private String model = "";
    private String mac = "";
    private String externalSnapshotUrl = "";
    private String internalSnapshotUrl = "";
    private String externalRtspUrl = "";
    private String internalRtspUrl = "";
    private String status = "";
    private boolean hasCredentials = false;
    private String thumbnailUrl;
    private String snapshotUrl;

    // Fields for edit camera
    private String internalHost = "";
    private String externalHost = "";
    private int internalHttp = 0;
    private int internalRtsp = 0;
    private int externalHttp = 0;
    private int externalRtsp = 0;

    public EvercamCamera()
    {

    }

    public EvercamCamera convertFromEvercam(io.evercam.Camera camera)
    {
        this.camera = camera;
        try
        {
            cameraId = camera.getId();
            name = camera.getName();
            if(AppData.defaultUser != null)
            {
                owner = AppData.defaultUser.getUsername();
            }
            realOwner = camera.getOwner();
            canEdit = camera.getRights().canEdit();
            canDelete = camera.getRights().canDelete();
            if(camera.hasCredentials())
            {
                hasCredentials = true;
                username = camera.getUsername();
                password = camera.getPassword();
            }
            timezone = camera.getTimezone();
            vendor = camera.getVendorName();
            model = camera.getModelName();
            mac = camera.getMacAddress();
            externalSnapshotUrl = camera.getExternalJpgUrl();
            internalSnapshotUrl = camera.getInternalJpgUrl();
            externalRtspUrl = camera.getExternalH264UrlWithCredential();
            internalRtspUrl = camera.getInternalH264UrlWithCredential();
            if(camera.isOnline())
            {
                status = CameraStatus.ACTIVE;
            }
            else
            {
                status = CameraStatus.OFFLINE;
            }
            internalHost = camera.getInternalHost();
            externalHost = camera.getExternalHost();
            internalHttp = camera.getInternalHttpPort();
            internalRtsp = camera.getInternalRtspPort();
            externalHttp = camera.getExternalHttpPort();
            externalRtsp = camera.getExternalRtspPort();
            thumbnailUrl = camera.getThumbnailUrl();
            snapshotUrl = camera.getLiveSnapshotUrl();
        }
        catch(EvercamException e)
        {
            Log.e(TAG, e.getMessage());
        }
        return this;
    }

    public int getId()
    {
        return id;
    }

    public void setId(int id)
    {
        this.id = id;
    }

    public String getStatus()
    {
        return status;
    }

    public boolean isActive()
    {
        if(getStatus().equalsIgnoreCase(CameraStatus.ACTIVE))
        {
            return true;
        }
        return false;
    }

    public boolean isOffline()
    {
        if(getStatus().equalsIgnoreCase(CameraStatus.OFFLINE))
        {
            return true;
        }
        return false;
    }

    public String getCameraId()
    {
        return cameraId;
    }

    public String getExternalSnapshotUrl()
    {
        return externalSnapshotUrl;
    }

    public String getInternalSnapshotUrl()
    {
        return internalSnapshotUrl;
    }

    public String getName()
    {
        return name;
    }

    public String getUsername()
    {
        return username;
    }

    public String getPassword()
    {
        return password;
    }

    public String getTimezone()
    {
        return timezone;
    }

    public String getModel()
    {
        return model;
    }

    public String getVendor()
    {
        return vendor;
    }

    public String getOwner()
    {
        return owner;
    }

    public String getRealOwner()
    {
        return realOwner;
    }

    public boolean canEdit()
    {
        return canEdit;
    }

    public boolean canDelete()
    {
        return canDelete;
    }

    public int getCanEditInt()
    {
        return canEdit() ? 1 : 0;
    }

    public int getCanDeleteInt()
    {
        return canDelete() ? 1 : 0;
    }

    public boolean hasCredentials()
    {
        return hasCredentials;
    }

    public int getHasCredentialsInt()
    {
        return hasCredentials() ? 1 : 0;
    }

    public void setCameraId(String cameraId)
    {
        this.cameraId = cameraId;
    }

    public void setExternalSnapshotUrl(String externalSnapshotUrl)
    {
        this.externalSnapshotUrl = externalSnapshotUrl;
    }

    public void setInternalSnapshotUrl(String internalSnapshotUrl)
    {
        this.internalSnapshotUrl = internalSnapshotUrl;
    }

    public void setName(String name)
    {
        this.name = name;
    }

    public void setUsername(String username)
    {
        this.username = username;
    }

    public void setPassword(String password)
    {
        this.password = password;
    }

    public void setTimezone(String timezone)
    {
        this.timezone = timezone;
    }

    public void setModel(String model)
    {
        this.model = model;
    }

    public void setVendor(String vendor)
    {
        this.vendor = vendor;
    }

    public void setOwner(String owner)
    {
        this.owner = owner;
    }

    public void setRealOwner(String realOwner)
    {
        this.realOwner = realOwner;
    }

    public void setCanEdit(boolean canEdit)
    {
        this.canEdit = canEdit;
    }

    public void setCanDelete(boolean canDelete)
    {
        this.canDelete = canDelete;
    }

    public String getMac()
    {
        return mac;
    }

    public void setMac(String mac)
    {
        this.mac = mac;
    }

    public String getExternalRtspUrl()
    {
        return externalRtspUrl;
    }

    public String getInternalRtspUrl()
    {
        return internalRtspUrl;
    }

    public void setExternalRtspUrl(String externalRtspUrl)
    {
        this.externalRtspUrl = externalRtspUrl;
    }

    public void setInternalRtspUrl(String internalRtspUrl)
    {
        this.internalRtspUrl = internalRtspUrl;
    }

    public void setStatus(String status)
    {
        this.status = status;
    }

    public String getCode()
    {
        return "";
    }

    public boolean isLocal()
    {
        return isLocal;
    }

    public void setLocal(boolean isLocal)
    {
        this.isLocal = isLocal;
    }

    public void setHasCredentials(boolean hasCredentials)
    {
        this.hasCredentials = hasCredentials;
    }

    public String getInternalHost()
    {
        return internalHost;
    }

    public String getExternalHost()
    {
        return externalHost;
    }

    public int getInternalHttp()
    {
        return internalHttp;
    }

    public int getInternalRtsp()
    {
        return internalRtsp;
    }

    public int getExternalHttp()
    {
        return externalHttp;
    }

    public int getExternalRtsp()
    {
        return externalRtsp;
    }

    public String getJpgPath()
    {
        try
        {
            // TODO: Wrap this in the wrapper or API response
            if(!internalSnapshotUrl.isEmpty())
            {
                return new URL(internalSnapshotUrl).getPath();
            }
            else if(!externalSnapshotUrl.isEmpty())
            {
                return new URL(externalSnapshotUrl).getPath();
            }
        }
        catch(MalformedURLException e)
        {
            Log.e(TAG, e.toString());
        }
        return "";
    }

    public String getH264Pash()
    {
        try
        {
            if(!internalRtspUrl.isEmpty())
            {
                return new URL(internalRtspUrl).getPath();
            }
            else if(!externalRtspUrl.isEmpty())
            {
                return new URL(externalRtspUrl).getPath();
            }
        }
        catch(MalformedURLException e)
        {
            Log.e(TAG, e.toString());
        }
        return "";
    }

    public void setInternalHost(String internalHost)
    {
        this.internalHost = internalHost;
    }

    public void setExternalHost(String externalHost)
    {
        this.externalHost = externalHost;
    }

    public void setInternalHttp(int internalHttp)
    {
        this.internalHttp = internalHttp;
    }

    public void setInternalRtsp(int internalRtsp)
    {
        this.internalRtsp = internalRtsp;
    }

    public void setExternalHttp(int externalHttp)
    {
        this.externalHttp = externalHttp;
    }

    public void setExternalRtsp(int externalRtsp)
    {
        this.externalRtsp = externalRtsp;
    }

    public String getThumbnailUrl()
    {
        return thumbnailUrl;
    }

    public void setThumbnailUrl(String thumbnailUrl)
    {
        this.thumbnailUrl = thumbnailUrl;
    }

    public String getSnapshotUrl()
    {
        return snapshotUrl;
    }

    public void setSnapshotUrl(String snapshotUrl)
    {
        this.snapshotUrl = snapshotUrl;
    }

    public boolean isHikvision()
    {
        if(getVendor().toLowerCase(Locale.UK).contains("hikvision"))
        {
            return true;
        }
        return false;
    }

    @Override
    public boolean equals(Object obj)
    {
        if(this == obj) return true;
        if(obj == null) return false;
        if(getClass() != obj.getClass()) return false;
        EvercamCamera other = (EvercamCamera) obj;
        if(cameraId.equals(other.cameraId) && externalRtspUrl.equals(other.externalRtspUrl) &&
                internalRtspUrl.equals(other.internalRtspUrl) && externalSnapshotUrl.equals(other
                .externalSnapshotUrl) && internalSnapshotUrl.equals(other.internalSnapshotUrl) &&
                mac.equals(other.mac) && model.equals(other.model) && name.equals(other.name) &&
                owner.equals(other.owner) && password.equals(other.password) && timezone.equals
                (other.timezone) && username.equals(other.username) && vendor.equals(other
                .vendor) && internalHost.equals(other.internalHost) && externalHost.equals(other
                .externalHost) && internalHttp == other.internalHttp && externalHttp == other
                .externalHttp && internalRtsp == other.internalRtsp && externalRtsp == other
                .externalRtsp && realOwner.equals(other.realOwner) && canEdit == other.canEdit &&
                canDelete == other.canDelete)
        {
            return true;
        }
        return false;
    }

    @Override
    public String toString()
    {
        return "EvercamCamera [loadingStatus=" + loadingStatus + ", id=" + id + ", " +
                "cameraId=" + cameraId + ", name=" + name + ", owner=" + owner + ", " +
                "realOwner=" + realOwner + ", canEdit=" + canEdit + ", " +
                "canDelete=" + canDelete + ", username=" + username + ", " +
                "password=" + password + ", timezone=" + timezone + ", vendor=" + vendor + ", " +
                "model=" + model + ", mac=" + mac + ", externalSnapshotUrl=" +
                externalSnapshotUrl + ", internalSnapshotUrl=" + internalSnapshotUrl + ", " +
                "externalRtspUrl=" + externalRtspUrl + ", internalRtspUrl=" + internalRtspUrl +
                ", status=" + status + ", hasCredentials=" + hasCredentials + ", " +
                "internalHost=" + internalHost + ", externalHost=" + externalHost + ", " +
                "internalHttp=" + internalHttp + ", internalRtsp=" + internalRtsp + ", " +
                "externalHttp=" + externalHttp + ", externalRtsp=" + externalRtsp + ", " +
                "thumbnailUrl=" + thumbnailUrl + "]";
    }
}
