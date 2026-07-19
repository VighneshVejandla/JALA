import { useState } from 'react';
import { useForm } from 'react-hook-form';
import { zodResolver } from '@hookform/resolvers/zod';
import { z } from 'zod';
import { Navigate, useLocation, useNavigate } from 'react-router-dom';
import { Droplet, Loader2, Eye, EyeOff } from 'lucide-react';
import { toast } from 'sonner';
import { useAuth } from '@/auth/AuthContext';
import { experienceOf, homePathFor } from '@/auth/roles';
import { ApiError } from '@/api/client';
import { Button } from '@/components/ui/button';
import {
  Card,
  CardContent,
  CardDescription,
  CardHeader,
  CardTitle,
} from '@/components/ui/card';
import {
  Form,
  FormControl,
  FormField,
  FormItem,
  FormLabel,
  FormMessage,
} from '@/components/ui/form';
import { Input } from '@/components/ui/input';

const schema = z.object({
  employeeCode: z.string().min(1, 'Employee code is required'),
  password: z.string().min(1, 'Password is required'),
});

type FormValues = z.infer<typeof schema>;

export function LoginPage() {
  const { isAuthenticated, experience, login } = useAuth();
  const navigate = useNavigate();
  const location = useLocation();
  const [submitting, setSubmitting] = useState(false);
  const [showPassword, setShowPassword] = useState(false);

  const form = useForm<FormValues>({
    resolver: zodResolver(schema),
    defaultValues: { employeeCode: '', password: '' },
  });

  if (isAuthenticated && experience) {
    return <Navigate to={homePathFor(experience)} replace />;
  }

  const onSubmit = async (values: FormValues) => {
    setSubmitting(true);
    try {
      const me = await login(values.employeeCode.trim(), values.password);
      const dest =
        (location.state as { from?: { pathname?: string } } | null)?.from
          ?.pathname ?? homePathFor(experienceOf(me.role));
      toast.success(`Welcome back, ${me.fullName.split(' ')[0]}`);
      navigate(dest, { replace: true });
    } catch (err) {
      const message =
        err instanceof ApiError && err.status === 401
          ? 'Invalid employee code or password.'
          : err instanceof Error
            ? err.message
            : 'Login failed. Please try again.';
      toast.error(message);
    } finally {
      setSubmitting(false);
    }
  };

  return (
    <div className="relative flex min-h-svh items-center justify-center overflow-hidden bg-background px-4">
      {/* Decorative background, matching the design language */}
      <div className="pointer-events-none absolute inset-0 -z-10">
        <div className="absolute -left-24 -top-24 h-72 w-72 -rotate-12 rounded-3xl bg-primary/10" />
        <div className="absolute -bottom-24 -right-24 h-72 w-72 rotate-12 rounded-3xl bg-primary/5" />
      </div>

      <Card className="w-full max-w-sm shadow-lg">
        <CardHeader className="items-center space-y-3 text-center">
          <div className="flex h-14 w-14 items-center justify-center rounded-2xl bg-primary/10">
            <Droplet className="h-7 w-7 text-primary" />
          </div>
          <div className="space-y-1">
            <CardTitle className="text-2xl">JALA Aqua Management</CardTitle>
            <CardDescription>
              Sign in with your employee credentials
            </CardDescription>
          </div>
        </CardHeader>
        <CardContent>
          <Form {...form}>
            <form
              onSubmit={form.handleSubmit(onSubmit)}
              className="space-y-4"
              noValidate
            >
              <FormField
                control={form.control}
                name="employeeCode"
                render={({ field }) => (
                  <FormItem>
                    <FormLabel>Employee Code</FormLabel>
                    <FormControl>
                      <Input
                        placeholder="e.g. EMP-1024"
                        autoComplete="username"
                        autoCapitalize="characters"
                        {...field}
                      />
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
                    <FormLabel>Password</FormLabel>
                    <FormControl>
                      <div className="relative">
                        <Input
                          type={showPassword ? 'text' : 'password'}
                          placeholder="••••••••"
                          autoComplete="current-password"
                          className="pr-10"
                          {...field}
                        />
                        <button
                          type="button"
                          onClick={() => setShowPassword((prev) => !prev)}
                          className="absolute inset-y-0 right-3 flex items-center text-muted-foreground hover:text-foreground"
                          aria-label={showPassword ? 'Hide password' : 'Show password'}
                        >
                          {showPassword ? (
                            <EyeOff className="h-4 w-4" />
                          ) : (
                            <Eye className="h-4 w-4" />
                          )}
                        </button>
                      </div>
                    </FormControl>
                    <FormMessage />
                  </FormItem>
                )}
              />
              <Button type="submit" className="w-full" disabled={submitting}>
                {submitting && <Loader2 className="mr-2 h-4 w-4 animate-spin" />}
                Sign in
              </Button>
            </form>
          </Form>
        </CardContent>
      </Card>
    </div>
  );
}
