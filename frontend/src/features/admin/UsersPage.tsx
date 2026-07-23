import { useState } from 'react';
import { useForm } from 'react-hook-form';
import { zodResolver } from '@hookform/resolvers/zod';
import { z } from 'zod';
import { KeyRound, MapPin, Pencil, Plus, UserRound } from 'lucide-react';
import { toast } from 'sonner';
import {
  useAssignSite,
  useCreateUser,
  useResetPassword,
  useRoles,
  useSetUserActive,
  useSites,
  useUpdateUser,
  useUserSites,
  useUsers,
} from '@/api/queries';
import type { UserResponse } from '@/api/types';
import { ROLE_LABELS } from '@/auth/roles';
import {
  EmptyBlock,
  ErrorBlock,
  LoadingBlock,
} from '@/components/common/StateViews';
import { Card, CardContent } from '@/components/ui/card';
import { Badge } from '@/components/ui/badge';
import { Button } from '@/components/ui/button';
import { Switch } from '@/components/ui/switch';
import { Avatar, AvatarFallback } from '@/components/ui/avatar';
import { Input } from '@/components/ui/input';
import {
  Dialog,
  DialogContent,
  DialogHeader,
  DialogTitle,
  DialogTrigger,
} from '@/components/ui/dialog';
import {
  Form,
  FormControl,
  FormField,
  FormItem,
  FormLabel,
  FormMessage,
} from '@/components/ui/form';
import {
  Select,
  SelectContent,
  SelectItem,
  SelectTrigger,
  SelectValue,
} from '@/components/ui/select';

const schema = z.object({
  fullName: z.string().min(1, 'Name is required'),
  employeeCode: z.string().min(1, 'Employee code is required'),
  roleId: z.string().min(1, 'Role is required'),
  email: z.string().email('Invalid email').or(z.literal('')).optional(),
  phone: z.string().optional(),
  password: z
    .string()
    .min(12, 'At least 12 characters')
    .regex(/[A-Za-z]/, 'Must contain a letter')
    .regex(/\d/, 'Must contain a digit'),
});
type FormValues = z.infer<typeof schema>;

function AddUserDialog() {
  const [open, setOpen] = useState(false);
  const roles = useRoles();
  const createUser = useCreateUser();
  const form = useForm<FormValues>({
    resolver: zodResolver(schema),
    defaultValues: {
      fullName: '',
      employeeCode: '',
      roleId: '',
      email: '',
      phone: '',
      password: '',
    },
  });

  const onSubmit = async (v: FormValues) => {
    try {
      await createUser.mutateAsync({
        fullName: v.fullName,
        employeeCode: v.employeeCode,
        roleId: v.roleId,
        email: v.email || undefined,
        phone: v.phone || undefined,
        password: v.password,
      });
      toast.success('User created');
      form.reset();
      setOpen(false);
    } catch (err) {
      toast.error(err instanceof Error ? err.message : 'Could not create user');
    }
  };

  return (
    <Dialog open={open} onOpenChange={setOpen}>
      <DialogTrigger asChild>
        <Button size="sm">
          <Plus className="mr-1 h-4 w-4" /> Add user
        </Button>
      </DialogTrigger>
      <DialogContent className="max-h-[90svh] overflow-y-auto">
        <DialogHeader>
          <DialogTitle>Add user</DialogTitle>
        </DialogHeader>
        <Form {...form}>
          <form onSubmit={form.handleSubmit(onSubmit)} className="space-y-3">
            <FormField
              control={form.control}
              name="fullName"
              render={({ field }) => (
                <FormItem>
                  <FormLabel>Full name</FormLabel>
                  <FormControl>
                    <Input placeholder="Jane Doe" {...field} />
                  </FormControl>
                  <FormMessage />
                </FormItem>
              )}
            />
            <FormField
              control={form.control}
              name="employeeCode"
              render={({ field }) => (
                <FormItem>
                  <FormLabel>Employee code</FormLabel>
                  <FormControl>
                    <Input placeholder="EMP-1024" {...field} />
                  </FormControl>
                  <FormMessage />
                </FormItem>
              )}
            />
            <FormField
              control={form.control}
              name="roleId"
              render={({ field }) => (
                <FormItem>
                  <FormLabel>Role</FormLabel>
                  <Select value={field.value} onValueChange={field.onChange}>
                    <FormControl>
                      <SelectTrigger>
                        <SelectValue placeholder="Select a role" />
                      </SelectTrigger>
                    </FormControl>
                    <SelectContent>
                      {(roles.data ?? []).map((r) => (
                        <SelectItem key={r.id} value={r.id}>
                          {r.name}
                        </SelectItem>
                      ))}
                    </SelectContent>
                  </Select>
                  <FormMessage />
                </FormItem>
              )}
            />
            <FormField
              control={form.control}
              name="email"
              render={({ field }) => (
                <FormItem>
                  <FormLabel>Email (optional)</FormLabel>
                  <FormControl>
                    <Input type="email" placeholder="jane@jala.com" {...field} />
                  </FormControl>
                  <FormMessage />
                </FormItem>
              )}
            />
            <FormField
              control={form.control}
              name="phone"
              render={({ field }) => (
                <FormItem>
                  <FormLabel>Phone (optional)</FormLabel>
                  <FormControl>
                    <Input placeholder="9990001111" {...field} />
                  </FormControl>
                  <FormMessage />
                </FormItem>
              )}
            />
            <FormField
              control={form.control}
              name="password"
              render={({ field }) => (
                <FormItem>
                  <FormLabel>Temporary password</FormLabel>
                  <FormControl>
                    <Input type="password" placeholder="min 12 chars" {...field} />
                  </FormControl>
                  <FormMessage />
                </FormItem>
              )}
            />
            <Button
              type="submit"
              className="w-full"
              disabled={createUser.isPending}
            >
              {createUser.isPending ? 'Creating…' : 'Create user'}
            </Button>
          </form>
        </Form>
      </DialogContent>
    </Dialog>
  );
}

const editSchema = z.object({
  fullName: z.string().min(1, 'Name is required'),
  roleId: z.string().optional(),
  email: z.string().email('Invalid email').or(z.literal('')).optional(),
  phone: z.string().optional(),
});
type EditValues = z.infer<typeof editSchema>;

function EditUserDialog({ user }: { user: UserResponse }) {
  const [open, setOpen] = useState(false);
  const roles = useRoles();
  const updateUser = useUpdateUser();
  const currentRoleId =
    roles.data?.find((r) => r.name === user.role)?.id ?? '';
  const form = useForm<EditValues>({
    resolver: zodResolver(editSchema),
    values: {
      fullName: user.fullName,
      roleId: currentRoleId,
      email: user.email ?? '',
      phone: user.phone ?? '',
    },
  });

  const onSubmit = async (v: EditValues) => {
    try {
      await updateUser.mutateAsync({
        id: user.id,
        body: {
          fullName: v.fullName,
          roleId: v.roleId || undefined,
          email: v.email || undefined,
          phone: v.phone || undefined,
        },
      });
      toast.success('User updated');
      setOpen(false);
    } catch (err) {
      toast.error(err instanceof Error ? err.message : 'Could not update user');
    }
  };

  return (
    <Dialog open={open} onOpenChange={setOpen}>
      <DialogTrigger asChild>
        <Button variant="ghost" size="icon" aria-label={`Edit ${user.fullName}`}>
          <Pencil className="h-4 w-4" />
        </Button>
      </DialogTrigger>
      <DialogContent className="max-h-[90svh] overflow-y-auto">
        <DialogHeader>
          <DialogTitle>Edit user</DialogTitle>
        </DialogHeader>
        <Form {...form}>
          <form onSubmit={form.handleSubmit(onSubmit)} className="space-y-3">
            <FormField
              control={form.control}
              name="fullName"
              render={({ field }) => (
                <FormItem>
                  <FormLabel>Full name</FormLabel>
                  <FormControl>
                    <Input {...field} />
                  </FormControl>
                  <FormMessage />
                </FormItem>
              )}
            />
            <FormField
              control={form.control}
              name="roleId"
              render={({ field }) => (
                <FormItem>
                  <FormLabel>Role</FormLabel>
                  <Select value={field.value} onValueChange={field.onChange}>
                    <FormControl>
                      <SelectTrigger>
                        <SelectValue placeholder="Select a role" />
                      </SelectTrigger>
                    </FormControl>
                    <SelectContent>
                      {(roles.data ?? []).map((r) => (
                        <SelectItem key={r.id} value={r.id}>
                          {r.name}
                        </SelectItem>
                      ))}
                    </SelectContent>
                  </Select>
                  <FormMessage />
                </FormItem>
              )}
            />
            <FormField
              control={form.control}
              name="email"
              render={({ field }) => (
                <FormItem>
                  <FormLabel>Email</FormLabel>
                  <FormControl>
                    <Input type="email" {...field} />
                  </FormControl>
                  <FormMessage />
                </FormItem>
              )}
            />
            <FormField
              control={form.control}
              name="phone"
              render={({ field }) => (
                <FormItem>
                  <FormLabel>Phone</FormLabel>
                  <FormControl>
                    <Input {...field} />
                  </FormControl>
                  <FormMessage />
                </FormItem>
              )}
            />
            <Button type="submit" className="w-full" disabled={updateUser.isPending}>
              {updateUser.isPending ? 'Saving…' : 'Save changes'}
            </Button>
          </form>
        </Form>
      </DialogContent>
    </Dialog>
  );
}

function ManageSitesDialog({ user }: { user: UserResponse }) {
  const [open, setOpen] = useState(false);
  const sites = useSites();
  const userSites = useUserSites(open ? user.id : null);
  const assign = useAssignSite(user.id);
  const assigned = new Set(userSites.data ?? []);

  return (
    <Dialog open={open} onOpenChange={setOpen}>
      <DialogTrigger asChild>
        <Button variant="ghost" size="icon" aria-label={`Manage sites for ${user.fullName}`}>
          <MapPin className="h-4 w-4" />
        </Button>
      </DialogTrigger>
      <DialogContent className="max-h-[90svh] overflow-y-auto">
        <DialogHeader>
          <DialogTitle>Assign sites — {user.fullName}</DialogTitle>
        </DialogHeader>
        {userSites.isLoading && <LoadingBlock label="Loading sites…" />}
        <div className="space-y-2">
          {(sites.data ?? []).map((s) => (
            <div
              key={s.id}
              className="flex items-center justify-between rounded-lg border border-border p-3"
            >
              <div className="min-w-0">
                <p className="truncate text-sm font-medium">{s.siteName}</p>
                <p className="truncate text-xs text-muted-foreground">{s.siteCode}</p>
              </div>
              <Switch
                checked={assigned.has(s.id)}
                disabled={assign.isPending || userSites.isLoading}
                aria-label={`Toggle ${s.siteName}`}
                onCheckedChange={(next) =>
                  assign.mutate(
                    { siteId: s.id, assign: next },
                    {
                      onError: (e) =>
                        toast.error(e instanceof Error ? e.message : 'Update failed'),
                    },
                  )
                }
              />
            </div>
          ))}
        </div>
      </DialogContent>
    </Dialog>
  );
}

const resetSchema = z.object({
  newPassword: z
    .string()
    .min(12, 'At least 12 characters')
    .regex(/[A-Za-z]/, 'Must contain a letter')
    .regex(/\d/, 'Must contain a digit'),
});
type ResetValues = z.infer<typeof resetSchema>;

function ResetPasswordDialog({ user }: { user: UserResponse }) {
  const [open, setOpen] = useState(false);
  const reset = useResetPassword();
  const form = useForm<ResetValues>({
    resolver: zodResolver(resetSchema),
    defaultValues: { newPassword: '' },
  });

  const onSubmit = async (v: ResetValues) => {
    try {
      await reset.mutateAsync({ id: user.id, body: { newPassword: v.newPassword } });
      toast.success('Password reset');
      form.reset();
      setOpen(false);
    } catch (err) {
      toast.error(err instanceof Error ? err.message : 'Could not reset password');
    }
  };

  return (
    <Dialog open={open} onOpenChange={setOpen}>
      <DialogTrigger asChild>
        <Button variant="ghost" size="icon" aria-label={`Reset password for ${user.fullName}`}>
          <KeyRound className="h-4 w-4" />
        </Button>
      </DialogTrigger>
      <DialogContent>
        <DialogHeader>
          <DialogTitle>Reset password — {user.fullName}</DialogTitle>
        </DialogHeader>
        <Form {...form}>
          <form onSubmit={form.handleSubmit(onSubmit)} className="space-y-3">
            <FormField
              control={form.control}
              name="newPassword"
              render={({ field }) => (
                <FormItem>
                  <FormLabel>New temporary password</FormLabel>
                  <FormControl>
                    <Input type="text" placeholder="min 12 chars" {...field} />
                  </FormControl>
                  <FormMessage />
                </FormItem>
              )}
            />
            <Button type="submit" className="w-full" disabled={reset.isPending}>
              {reset.isPending ? 'Resetting…' : 'Reset password'}
            </Button>
          </form>
        </Form>
      </DialogContent>
    </Dialog>
  );
}

export function UsersPage() {
  const { data, isLoading, isError, refetch } = useUsers();
  const setActive = useSetUserActive();

  return (
    <div className="space-y-4">
      <div className="flex items-center justify-between">
        <h2 className="text-lg font-semibold">Users</h2>
        <AddUserDialog />
      </div>

      {isLoading && <LoadingBlock label="Loading users…" />}
      {isError && (
        <ErrorBlock message="Could not load users." onRetry={() => refetch()} />
      )}
      {data && data.length === 0 && (
        <EmptyBlock icon={<UserRound className="h-6 w-6" />} title="No users" />
      )}

      <div className="space-y-3">
        {data?.map((u) => (
          <Card key={u.id}>
            <CardContent className="flex items-center gap-3 p-4">
              <Avatar className="h-10 w-10 border border-border">
                <AvatarFallback className="bg-primary/10 text-sm font-semibold text-primary">
                  {u.fullName
                    .split(' ')
                    .map((p) => p[0])
                    .filter(Boolean)
                    .slice(0, 2)
                    .join('')
                    .toUpperCase()}
                </AvatarFallback>
              </Avatar>
              <div className="min-w-0 flex-1">
                <p className="truncate font-medium">{u.fullName}</p>
                <p className="truncate text-xs text-muted-foreground">
                  {u.employeeCode}
                  {u.email && ` · ${u.email}`}
                </p>
                <Badge variant="secondary" className="mt-1">
                  {ROLE_LABELS[u.role]}
                </Badge>
              </div>
              <div className="flex flex-col items-end gap-1">
                <div className="flex items-center gap-2">
                  <span className="text-xs text-muted-foreground">
                    {u.isActive ? 'Active' : 'Inactive'}
                  </span>
                  <Switch
                    checked={u.isActive}
                    disabled={setActive.isPending}
                    aria-label={u.isActive ? 'Deactivate user' : 'Activate user'}
                    onCheckedChange={(next) =>
                      setActive.mutate(
                        { id: u.id, active: next },
                        {
                          onError: (e) =>
                            toast.error(
                              e instanceof Error ? e.message : 'Update failed',
                            ),
                        },
                      )
                    }
                  />
                </div>
                <div className="flex items-center">
                  <EditUserDialog user={u} />
                  <ManageSitesDialog user={u} />
                  <ResetPasswordDialog user={u} />
                </div>
              </div>
            </CardContent>
          </Card>
        ))}
      </div>
    </div>
  );
}
