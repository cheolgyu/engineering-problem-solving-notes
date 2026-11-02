    public static XaiAssistantPolicyDecision evaluateNarrativeClaims(
            XaiEvidencePackage evidencePackage,
            List<String> citedMetricKeys,
            List<String> citedEvidenceRefs
    ) {
        if (evidencePackage == null) {
            return new XaiAssistantPolicyDecision(false, "assistant_grounding_evidence_missing");
        }
        if (citedMetricKeys == null || citedMetricKeys.isEmpty()) {
            return new XaiAssistantPolicyDecision(false, "assistant_grounding_metric_citation_missing");
        }
        if (citedEvidenceRefs == null || citedEvidenceRefs.isEmpty()) {
            return new XaiAssistantPolicyDecision(false, "assistant_grounding_evidence_ref_missing");
        }
        for (String metricKey : citedMetricKeys) {
            if (!evidencePackage.containsMetricKey(metricKey)) {
                return new XaiAssistantPolicyDecision(false, "assistant_grounding_unknown_metric:" + metricKey);
            }
        }
        for (String evidenceRef : citedEvidenceRefs) {
            if (!evidencePackage.containsEvidenceRef(evidenceRef)) {
                return new XaiAssistantPolicyDecision(false, "assistant_grounding_unknown_evidence:" + evidenceRef);
            }
        }
        return new XaiAssistantPolicyDecision(true, "assistant_grounding_allowed");
    }
