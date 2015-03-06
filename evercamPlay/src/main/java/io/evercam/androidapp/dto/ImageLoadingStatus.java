package io.evercam.androidapp.dto;

public enum ImageLoadingStatus
{
    not_started, live_requested_sent, live_received, live_not_received, localNetwork_requested,
    localNetwork_received, localNetwork_not_received, thumbnail_not_received, thumbnail_received
}
