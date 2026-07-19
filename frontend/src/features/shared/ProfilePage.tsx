import { LogOut, Mail, Phone, ShieldCheck } from 'lucide-react';
import { useAuth } from '@/auth/AuthContext';
import { ROLE_LABELS } from '@/auth/roles';
import { Button } from '@/components/ui/button';
import {
  Avatar,
  AvatarFallback,
} from '@/components/ui/avatar';
import { Card, CardContent } from '@/components/ui/card';
import { Badge } from '@/components/ui/badge';

export function ProfilePage() {
  const { user, logout } = useAuth();
  if (!user) return null;

  const initials = user.fullName
    .split(' ')
    .map((p) => p[0])
    .filter(Boolean)
    .slice(0, 2)
    .join('')
    .toUpperCase();

  return (
    <div className="space-y-4">
      <Card>
        <CardContent className="flex flex-col items-center gap-3 p-6 text-center">
          <Avatar className="h-20 w-20 border-2 border-primary/20">
            <AvatarFallback className="bg-primary/10 text-2xl font-semibold text-primary">
              {initials}
            </AvatarFallback>
          </Avatar>
          <div>
            <p className="text-lg font-semibold">{user.fullName}</p>
            <p className="text-sm text-muted-foreground">{user.employeeCode}</p>
          </div>
          <Badge className="gap-1">
            <ShieldCheck className="h-3.5 w-3.5" />
            {ROLE_LABELS[user.role]}
          </Badge>
        </CardContent>
      </Card>

      <Card>
        <CardContent className="divide-y divide-border p-0">
          <Row icon={<Mail className="h-4 w-4" />} label="Email" value={user.email ?? '—'} />
          <Row icon={<Phone className="h-4 w-4" />} label="Phone" value={user.phone ?? '—'} />
        </CardContent>
      </Card>

      <Button variant="outline" className="w-full" onClick={logout}>
        <LogOut className="mr-2 h-4 w-4" /> Sign out
      </Button>
    </div>
  );
}

function Row({
  icon,
  label,
  value,
}: {
  icon: React.ReactNode;
  label: string;
  value: string;
}) {
  return (
    <div className="flex items-center gap-3 p-4">
      <span className="text-muted-foreground">{icon}</span>
      <span className="flex-1 text-sm text-muted-foreground">{label}</span>
      <span className="text-sm font-medium">{value}</span>
    </div>
  );
}
