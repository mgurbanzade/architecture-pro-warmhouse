package services

import (
	"bytes"
	"encoding/json"
	"fmt"
	"net/http"
	"time"

	"smarthome/models"
)

type DeviceRegistryClient struct {
	BaseURL    string
	HouseID    string
	HTTPClient *http.Client
}

func NewDeviceRegistryClient(baseURL, houseID string) *DeviceRegistryClient {
	return &DeviceRegistryClient{
		BaseURL: baseURL,
		HouseID: houseID,
		HTTPClient: &http.Client{
			Timeout: 2 * time.Second,
		},
	}
}

func (c *DeviceRegistryClient) Enabled() bool {
	return c != nil && c.BaseURL != ""
}

func SerialNumber(sensorID int) string {
	return fmt.Sprintf("monolith-sensor-%d", sensorID)
}

func (c *DeviceRegistryClient) RegisterSensor(s models.Sensor) error {
	if !c.Enabled() {
		return nil
	}

	body, err := json.Marshal(map[string]string{
		"houseId":      c.HouseID,
		"typeCode":     string(s.Type),
		"serialNumber": SerialNumber(s.ID),
		"name":         s.Name,
	})
	if err != nil {
		return fmt.Errorf("error encoding device: %w", err)
	}

	resp, err := c.HTTPClient.Post(c.BaseURL+"/devices", "application/json", bytes.NewReader(body))
	if err != nil {
		return fmt.Errorf("error calling device service: %w", err)
	}
	defer resp.Body.Close()

	switch resp.StatusCode {
	case http.StatusCreated, http.StatusConflict:
		return nil
	default:
		return fmt.Errorf("unexpected status code from device service: %d", resp.StatusCode)
	}
}
