package com.mecano.notification_service.service;

import com.mecano.notification_service.dto.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class NotificationService {

    private final EmailService emailService;

    // ── Inscription ─────────────────────────────────────────────
    public void notifyUserRegistered(UserRegisteredEvent event) {
        String subject = "🔧 Bienvenue sur Mecano !";
        String html = """
            <div style="font-family:Arial,sans-serif;max-width:600px;margin:auto">
              <h2 style="color:#f97316">🔧 Bienvenue sur Mecano !</h2>
              <p>Bonjour <strong>%s</strong>,</p>
              <p>Votre compte <strong>%s</strong> a été créé avec succès.</p>
              %s
              <hr/>
              <p style="color:#888;font-size:12px">L'équipe Mecano</p>
            </div>
            """.formatted(
                event.getFirstName(),
                event.getRole().equals("MECHANIC") ? "mécanicien" : "automobiliste",
                event.getRole().equals("MECHANIC")
                    ? "<p>Commencez par choisir votre abonnement pour apparaître dans les recherches.</p>"
                    : "<p>En cas de panne, recherchez un mécanicien près de vous !</p>"
        );
        emailService.sendHtmlEmail(event.getEmail(), subject, html);
    }

    // ── Paiement confirmé ───────────────────────────────────────
    public void notifyPaymentConfirmed(PaymentConfirmedEvent event) {
        String subject = "✅ Abonnement " + event.getPlanLevel() + " activé !";
        String html = """
            <div style="font-family:Arial,sans-serif;max-width:600px;margin:auto">
              <h2 style="color:#10b981">✅ Paiement confirmé</h2>
              <p>Bonjour <strong>%s</strong>,</p>
              <p>Votre abonnement <strong>%s</strong> est maintenant actif.</p>
              <table style="border-collapse:collapse;width:100%">
                <tr style="background:#f3f4f6">
                  <td style="padding:8px;border:1px solid #e5e7eb">Plan</td>
                  <td style="padding:8px;border:1px solid #e5e7eb"><strong>%s</strong></td>
                </tr>
                <tr>
                  <td style="padding:8px;border:1px solid #e5e7eb">Montant</td>
                  <td style="padding:8px;border:1px solid #e5e7eb">%s %s</td>
                </tr>
              </table>
              <p>Vous apparaissez désormais en priorité dans votre zone !</p>
              <hr/>
              <p style="color:#888;font-size:12px">L'équipe Mecano</p>
            </div>
            """.formatted(
                event.getFirstName(),
                event.getPlanLevel(),
                event.getPlanLevel(),
                event.getAmount(),
                event.getCurrency()
        );
        emailService.sendHtmlEmail(event.getEmail(), subject, html);
    }

    // ── Demande de dépannage ───────────────────────────────────
    public void notifyRepairRequested(RepairRequestedEvent event) {
        String htmlAuto = """
            <div style="font-family:Arial,sans-serif;max-width:600px;margin:auto">
              <h2 style="color:#3b82f6">🚗 Demande de dépannage envoyée</h2>
              <p>Bonjour <strong>%s</strong>,</p>
              <p>Votre demande a été transmise au mécanicien <strong>%s</strong>.</p>
              <p><em>Description : %s</em></p>
              <p>Coordonnées : lat=%s / lng=%s</p>
              <hr/>
              <p style="color:#888;font-size:12px">L'équipe Mecano</p>
            </div>
            """.formatted(
                event.getAutomobilistFirstName(),
                event.getMechanicFirstName(),
                event.getDescription(),
                event.getLatitude(),
                event.getLongitude()
        );
        emailService.sendHtmlEmail(event.getAutomobilistEmail(),
                "🚗 Demande de dépannage envoyée", htmlAuto);

        String htmlMeca = """
            <div style="font-family:Arial,sans-serif;max-width:600px;margin:auto">
              <h2 style="color:#f97316">🔧 Nouvelle demande de dépannage !</h2>
              <p>Bonjour <strong>%s</strong>,</p>
              <p><strong>%s</strong> a besoin de votre aide.</p>
              <p><em>%s</em></p>
              <p>📍 Position : lat=%s / lng=%s</p>
              <p>Connectez-vous à l'application pour accepter la demande.</p>
              <hr/>
              <p style="color:#888;font-size:12px">L'équipe Mecano</p>
            </div>
            """.formatted(
                event.getMechanicFirstName(),
                event.getAutomobilistFirstName(),
                event.getDescription(),
                event.getLatitude(),
                event.getLongitude()
        );
        emailService.sendHtmlEmail(event.getMechanicEmail(),
                "🔧 Nouvelle demande de dépannage", htmlMeca);
    }

    // ── Abonnement expiré ───────────────────────────────────────
    public void notifySubscriptionExpired(SubscriptionExpiredEvent event) {
        String subject = "⚠️ Votre abonnement " + event.getPlanLevel() + " a expiré";
        String html = """
            <div style="font-family:Arial,sans-serif;max-width:600px;margin:auto">
              <h2 style="color:#ef4444">⚠️ Abonnement expiré</h2>
              <p>Bonjour <strong>%s</strong>,</p>
              <p>Votre abonnement <strong>%s</strong> a expiré.</p>
              <p>Renouvelez-le pour continuer à apparaître en priorité.</p>
              <a href="http://localhost:3000/subscription"
                 style="background:#f97316;color:white;padding:10px 20px;
                        border-radius:5px;text-decoration:none">
                Renouveler mon abonnement
              </a>
              <hr/>
              <p style="color:#888;font-size:12px">L'équipe Mecano</p>
            </div>
            """.formatted(event.getFirstName(), event.getPlanLevel());
        emailService.sendHtmlEmail(event.getEmail(), subject, html);
    }
}
