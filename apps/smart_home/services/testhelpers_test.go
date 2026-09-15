package services

import (
	"encoding/json"
	"net/http"
	"testing"
)

func decodeJSON(t *testing.T, r *http.Request, into any) {
	t.Helper()
	if err := json.NewDecoder(r.Body).Decode(into); err != nil {
		t.Fatalf("failed to decode request body: %v", err)
	}
}
