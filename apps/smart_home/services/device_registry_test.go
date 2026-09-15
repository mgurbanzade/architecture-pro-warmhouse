package services

import (
	"net/http"
	"net/http/httptest"
	"testing"

	"smarthome/models"
)

func sensor() models.Sensor {
	return models.Sensor{ID: 7, Name: "Living Room Temperature", Type: models.Temperature}
}

func TestRegisterSensorIsNoopWhenDisabled(t *testing.T) {
	client := NewDeviceRegistryClient("", "house")
	if err := client.RegisterSensor(sensor()); err != nil {
		t.Fatalf("expected nil error for disabled client, got %v", err)
	}
}

func TestRegisterSensorSendsSerialAndTreatsCreatedAsSuccess(t *testing.T) {
	var got map[string]string
	server := httptest.NewServer(http.HandlerFunc(func(w http.ResponseWriter, r *http.Request) {
		if r.URL.Path != "/devices" || r.Method != http.MethodPost {
			t.Errorf("unexpected request %s %s", r.Method, r.URL.Path)
		}
		decodeJSON(t, r, &got)
		w.WriteHeader(http.StatusCreated)
	}))
	defer server.Close()

	client := NewDeviceRegistryClient(server.URL, "house-1")
	if err := client.RegisterSensor(sensor()); err != nil {
		t.Fatalf("unexpected error: %v", err)
	}
	if got["serialNumber"] != "monolith-sensor-7" || got["houseId"] != "house-1" || got["typeCode"] != "temperature" {
		t.Fatalf("unexpected body: %v", got)
	}
}

func TestRegisterSensorTreatsConflictAsAlreadyRegistered(t *testing.T) {
	server := httptest.NewServer(http.HandlerFunc(func(w http.ResponseWriter, r *http.Request) {
		w.WriteHeader(http.StatusConflict)
	}))
	defer server.Close()

	if err := NewDeviceRegistryClient(server.URL, "house").RegisterSensor(sensor()); err != nil {
		t.Fatalf("409 must not be an error, got %v", err)
	}
}

func TestRegisterSensorReportsServerErrors(t *testing.T) {
	server := httptest.NewServer(http.HandlerFunc(func(w http.ResponseWriter, r *http.Request) {
		w.WriteHeader(http.StatusInternalServerError)
	}))
	defer server.Close()

	if err := NewDeviceRegistryClient(server.URL, "house").RegisterSensor(sensor()); err == nil {
		t.Fatal("expected error for 500")
	}
}

func TestRegisterSensorReportsUnreachableService(t *testing.T) {
	if err := NewDeviceRegistryClient("http://127.0.0.1:1", "house").RegisterSensor(sensor()); err == nil {
		t.Fatal("expected error for unreachable service")
	}
}
