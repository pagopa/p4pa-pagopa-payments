# Github
data "github_organization_teams" "all" {
  root_teams_only = true
  summary_only    = true
}

# KV Core
data "azurerm_key_vault" "key_vault_core" {
  name                = "${var.prefix}-${var.env_short}-${var.location_short}-core-kv"
  resource_group_name = "${var.prefix}-${var.env_short}-${var.location_short}-core-sec-rg"
}

# Kv Domain
data "azurerm_key_vault" "key_vault_domain" {
  name                = "${var.prefix}-${var.env_short}-${var.domain}-kv"
  resource_group_name = "${var.prefix}-${var.env_short}-${var.location_short}-${var.domain}-sec-rg"
}

# Key Vault - Sonar Token
data "azurerm_key_vault_secret" "sonar_token" {
  count = var.env_short == "p" ? 1 : 0

  key_vault_id = data.azurerm_key_vault.key_vault_core.id
  name         = "sonar-cloud-token"
}

# Key Vault - Slack webhok
data "azurerm_key_vault_secret" "slack_webhook" {
  count = var.env_short == "p" ? 1 : 0

  key_vault_id = data.azurerm_key_vault.key_vault_core.id
  name         = "slack-webhook-url"
}
